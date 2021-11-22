/* Copyright 2021 Esri
 *
 * Licensed under the Apache License Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Collections.Generic;
using System.Configuration;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Grpc.Net.Client;
using Google.Protobuf.WellKnownTypes;

namespace gRPC_Sender
{
    class Program
    {
        
        private static string gRPC_endpoint_URL = ConfigurationManager.AppSettings["gRPC_endpoint_URL"];
        private static string gRPC_endpoint_header_path = ConfigurationManager.AppSettings["gRPC_endpoint_header_path"];
        private static string gRPC_endpoint_header_path_key = ConfigurationManager.AppSettings["gRPC_endpoint_header_path_key"];
        private static int gRPC_endpoint_URL_port = Int32.Parse(ConfigurationManager.AppSettings["gRPC_endpoint_URL_port"]);
        private static bool streamData = Boolean.Parse(ConfigurationManager.AppSettings["streamData"]);
        private static bool authenticationArcGIS = Boolean.Parse(ConfigurationManager.AppSettings["authenticationArcGIS"]);
        private static string tokenPortalUrl = ConfigurationManager.AppSettings["tokenPortalUrl"];
        private static string username = ConfigurationManager.AppSettings["username"];
        private static string password = ConfigurationManager.AppSettings["password"];
        private static string fileUrl = ConfigurationManager.AppSettings["fileUrl"];
        private static bool hasHeaderRow = Boolean.Parse(ConfigurationManager.AppSettings["hasHeaderRow"]);
        private static string fieldDelimiter = ConfigurationManager.AppSettings["fieldDelimiter"];
        private static int numLinesPerBatch = Int32.Parse(ConfigurationManager.AppSettings["numLinesPerBatch"]);
        private static long sendInterval = Int32.Parse(ConfigurationManager.AppSettings["sendInterval"]);
        private static int timeField = Int32.Parse(ConfigurationManager.AppSettings["timeField"]);
        private static bool setToCurrentTime = Boolean.Parse(ConfigurationManager.AppSettings["setToCurrentTime"]);
        private static string dateFormat = ConfigurationManager.AppSettings["dateFormat"];
        private static CultureInfo dateCulture = CultureInfo.CreateSpecificCulture(ConfigurationManager.AppSettings["dateCulture"]);
        private static long iterationLimit = Int64.Parse(ConfigurationManager.AppSettings["iterationLimit"]);
        private static int tokenExpiry = Int32.Parse(ConfigurationManager.AppSettings["tokenExpiry"]); 

                
        static async Task Main()
        {           
            

            Grpc.Core.AsyncClientStreamingCall<Request, Response> call = null;
            Request request = new Request();            
            Response response = new Response();
            //string responseString;

            int featuresInBatchCount = 0;
            int totalFeaturesSentCount = 0;
            
            double maxIterations = iterationLimit;            
            if(iterationLimit < 1) 
                maxIterations = double.PositiveInfinity;

            int iterationCount = 0;

            string line;
            DateTime batchStartTime = DateTime.MinValue;

            try
            {
                string[] contentArray = readFile();
                if (hasHeaderRow){
                    contentArray = contentArray.Where((source, index) => index != 0).ToArray();
                }
                int lineCount = contentArray.Length;



                using var channel = GrpcChannel.ForAddress(String.Format("https://{0}:{1}", gRPC_endpoint_URL, gRPC_endpoint_URL_port));
                var grpcClient = new GrpcFeed.GrpcFeedClient(channel); 

                var metadata = new Grpc.Core.Metadata
                {
                    { gRPC_endpoint_header_path_key, gRPC_endpoint_header_path }
                };

                if (authenticationArcGIS){
                    string token = await getToken(tokenPortalUrl,username,password);                     
                    if (token == "")
                        return;
                    metadata.Add("authorization", $"Bearer {token}");                    
                }  

                if (streamData){
                    call = grpcClient.stream(metadata);
                }

                                
                while (iterationCount < maxIterations)
                {
                    for (int l = 0; l < lineCount; l++)
                    {                        
                        line = contentArray[l];
                        if (String.IsNullOrEmpty(line)){
                            continue;
                        }

                        
                        if (request.Features.Count == 0)  
                            batchStartTime = DateTime.UtcNow;
                        
                        
                        dynamic[] values = line.Split(fieldDelimiter);
                        
                        if (setToCurrentTime)
                        {
                            if (String.IsNullOrEmpty(dateFormat))
                            {
                                string dt = new DateTimeOffset(DateTime.Now).ToUnixTimeMilliseconds().ToString();
                                values[timeField] = dt;
                            }
                            else
                            {
                                try{
                                    string dt = DateTime.Now.ToString(dateFormat,dateCulture);
                                    values[timeField] = dt;
                                }
                                catch(Exception e){
                                    string dt = new DateTimeOffset(DateTime.Now).ToUnixTimeMilliseconds().ToString();
                                    values[timeField] = dt;
                                }
                            }
                        }

                     
                        Feature feature = new Feature();

                        foreach (var value in values)
                        {        
                            long longVal = 0;
                            double flVal = 0;
                            bool boolVal = false;
                            bool isLong = long.TryParse(value, out longVal);
                            bool isFloat = double.TryParse(value, out flVal);
                            bool isBool = Boolean.TryParse(value, out boolVal);
                            
                            if (isBool)
                                feature.Attributes.Add(Any.Pack(new BoolValue() { Value = boolVal }));
                            else if ( isLong )
                                feature.Attributes.Add(Any.Pack(new Int64Value() { Value = longVal }));
                            else if (isFloat)           
                                feature.Attributes.Add(Any.Pack(new DoubleValue() { Value = flVal }));
                            else
                                feature.Attributes.Add(Any.Pack(new StringValue() { Value = value }));

                        }    

                        request.Features.Add(feature);                  
                        
                        featuresInBatchCount++;
                        totalFeaturesSentCount++;


                        if (featuresInBatchCount == numLinesPerBatch || totalFeaturesSentCount == lineCount)
                        {                           
                            // send the batch of events to the gRPC receiver                           
                            //if the request fails because the token expired, get a new one and retry the request
                            
                            try{

                                if (!streamData){                                    
                                    response = await grpcClient.sendAsync(request, metadata);
                                }
                                else{                                   
                                    await call.RequestStream.WriteAsync(request);
                                } 

                                long elapsedTime = Convert.ToInt64((DateTime.UtcNow - batchStartTime).TotalMilliseconds);   
                                if (elapsedTime < sendInterval){                               
                                    Thread.Sleep((int)(sendInterval - elapsedTime));
                                }
                                
                            }
                            catch(Grpc.Core.RpcException rpcEx){
                                 if (rpcEx.StatusCode == Grpc.Core.StatusCode.PermissionDenied && authenticationArcGIS){ 
                                    string token = await getToken(tokenPortalUrl,username,password);                     
                                    if (token == "")
                                        return;                              
                                    metadata[1] = new Grpc.Core.Metadata.Entry("authorization", $"Bearer {token}");
                                    response = await grpcClient.sendAsync(request, metadata);      
                                 }
                            }
                            catch (Exception e){
                                Console.WriteLine(e.Message);
                                Console.WriteLine(e.StackTrace);
                                Console.WriteLine(e.Data);
                            }
                            finally{
                                request.Features.Clear();
                                featuresInBatchCount = 0;
                            }                           
                        }
                    }
                   
                    iterationCount++;
                }

                if (streamData){
                    await call.RequestStream.CompleteAsync();
                    response = await call;
                }
                
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                Console.WriteLine(e.Data);
            }
            finally{
                if (streamData){
                    await call.RequestStream.CompleteAsync();
                    response = await call;
                }
                Console.WriteLine($"Completed. {totalFeaturesSentCount} sent.");
            }
        }

        static string[] readFile(){

            Console.WriteLine($"Fetching and reading file: {fileUrl}");

            HttpWebRequest myHttpWebRequest = (HttpWebRequest)WebRequest.Create(fileUrl);
            // Sends the HttpWebRequest and waits for the response.			
            HttpWebResponse myHttpWebResponse = (HttpWebResponse)myHttpWebRequest.GetResponse();
            // Gets the stream associated with the response.
            Stream receiveStream = myHttpWebResponse.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            // Pipes the stream to a higher level stream reader with the required encoding format. 
            StreamReader readStream = new StreamReader(receiveStream, encode);
            
            

            // Read lines from the file until the end of 
            // the file is reached.
            string[] contentArray = readStream.ReadToEnd().Replace("\r", "").Split('\n');

            readStream.Close();

            return contentArray;

        }

        static async Task<string> getToken(string url, string user, string pass)
        {               
                
            Console.WriteLine("Fetching a new token");

            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Accept", "*/*");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Referer", "http://localhost:8888");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Content-Type", "application/json; charset=utf-8");

            try
            {        
                var values = new Dictionary<string, string>
                {
                    { "username", user },
                    { "password", pass },
                    { "client", "referer" },
                    { "referer", "http://localhost:8888"},
                    { "f", "json"},
                    { "expiration", tokenExpiry.ToString()}
                };
                
                var content = new FormUrlEncodedContent(values);
                var response = await httpClient.PostAsync($"{url}/sharing/rest/generateToken", content);            
                var responseString = await response.Content.ReadAsStringAsync();
                dynamic tokenJson = JsonConvert.DeserializeObject(responseString); 
                string token = tokenJson["token"];                

                return token;
            }
            catch (Exception e)
            {
                Console.Out.WriteLine("getToken Error: " + e.Message);
                return "";
            }
        }
    }
}
