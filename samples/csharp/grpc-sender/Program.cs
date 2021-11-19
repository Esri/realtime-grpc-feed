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
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Grpc.Net.Client;
using GrpcProto;
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
        private static int sendInterval = Int32.Parse(ConfigurationManager.AppSettings["sendInterval"]);
        private static int timeField = Int32.Parse(ConfigurationManager.AppSettings["timeField"]);
        private static bool setToCurrentTime = Boolean.Parse(ConfigurationManager.AppSettings["setToCurrentTime"]);
        private static string dateFormat = ConfigurationManager.AppSettings["dateFormat"];
        private static CultureInfo dateCulture = CultureInfo.CreateSpecificCulture(ConfigurationManager.AppSettings["dateCulture"]);
        private static bool repeatSimulation = Boolean.Parse(ConfigurationManager.AppSettings["repeatSimulation"]);

        private static readonly HttpClient httpClient = new HttpClient();
        
        static async Task Main()
        {
            Console.WriteLine("Starting...");
            Console.WriteLine($"Fetching and reading file: {fileUrl}");
            try
            {   
                
                HttpWebRequest myHttpWebRequest = (HttpWebRequest)WebRequest.Create(fileUrl);
                // Sends the HttpWebRequest and waits for the response.			
                HttpWebResponse myHttpWebResponse = (HttpWebResponse)myHttpWebRequest.GetResponse();
                // Gets the stream associated with the response.
                Stream receiveStream = myHttpWebResponse.GetResponseStream();
                Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
                // Pipes the stream to a higher level stream reader with the required encoding format. 
                StreamReader readStream = new StreamReader(receiveStream, encode);
                string line;
                //string headerLine;
                //string[] fields = null;
                string token = "";
                //JObject schema =  new JObject();

                // Read lines from the file until the end of 
                // the file is reached.
                string[] contentArray = readStream.ReadToEnd().Replace("\r", "").Split('\n');

                readStream.Close();

                //int c = contentArray.Length;
                bool runTask = true;


                using var channel = GrpcChannel.ForAddress(String.Format("https://{0}:{1}", gRPC_endpoint_URL, gRPC_endpoint_URL_port));
                var grpcClient = new GrpcFeed.GrpcFeedClient(channel);
                
                

                var metadata = new Grpc.Core.Metadata
                {
                    { gRPC_endpoint_header_path_key, gRPC_endpoint_header_path }
                };


                
                
                if (hasHeaderRow){
                    contentArray = contentArray.Where((source, index) => index != 0).ToArray();
                }
                int c = contentArray.Length;

                httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Accept", "*/*");
                httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Referer", "http://localhost:8888");
                httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Content-Type", "application/json; charset=utf-8");
                
                if (authenticationArcGIS){
                    string tokenStr = await getToken(tokenPortalUrl,username,password,21600);                     
                    if (tokenStr.Contains("Unable to generate token.")){
                        Console.WriteLine(tokenStr);
                        return;
                    }                 
                    dynamic tokenJson = JsonConvert.DeserializeObject(tokenStr); 
                    token = tokenJson["token"];               
                    metadata.Add("authorization", $"Bearer {token}");                    
                }
                                            
                              
                
                int count = 0;
                int countTotal = 0;
                Request request = new Request();

                var stopwatch = new Stopwatch();
                var taskStopwatch = new Stopwatch();
                while (runTask)
                {
                    taskStopwatch.Start();
                    for (int l = 0; l < c; l++)
                    {                        
                        line = contentArray[l];
                        if (String.IsNullOrEmpty(line)){
                            continue;
                        }
                        //Console.WriteLine($"Line: {line}");

                        
                        if (request.Features.Count == 0)                        
                            stopwatch.Start();
                        
                        
                        dynamic[] values = line.Split(fieldDelimiter);
                        
                        if (setToCurrentTime)
                        {
                            if (String.IsNullOrEmpty(dateFormat))
                            {
                                //Console.WriteLine("setting time value");
                                string dt = new DateTimeOffset(DateTime.Now).ToUnixTimeMilliseconds().ToString();
                                values[timeField] = dt;
                                //Console.WriteLine("done setting time value");
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
                            decimal decVal = 0;
                            bool boolVal = false;
                            bool isLong = long.TryParse(value, out longVal);
                            bool isDec = decimal.TryParse(value, out decVal);
                            bool isBool = Boolean.TryParse(value, out boolVal);
                            
                            if (isBool)
                                feature.Attributes.Add(Any.Pack(new BoolValue() { Value = boolVal }));
                            else if ( isLong )
                                feature.Attributes.Add(Any.Pack(new Int64Value() { Value = longVal }));
                            else if (isDec)           
                                feature.Attributes.Add(Any.Pack(new FloatValue() { Value = (float)decVal }));
                            else
                                feature.Attributes.Add(Any.Pack(new StringValue() { Value = value }));

                        }    

                        request.Features.Add(feature);   

                 
                        
                        count++;
                        countTotal++;

                        
                        if (count == numLinesPerBatch || countTotal == c)
                        {                           
                            // send the batch of events to the gRPC receiver                           
                            //if the request fails because the token expired, get a new one and retry the request
                            try{
                                Response response;
                                string responseString;
                                if (!streamData){
                                    response = await grpcClient.sendAsync(request, metadata);
                                    responseString = response.Message;
                                    Console.WriteLine($"gRPC feed response: {responseString}");
                                    if (response.Code == 7 && authenticationArcGIS){                                    
                                        Console.WriteLine($"Renewing the token for {username}");
                                        string tokenStr = await getToken(tokenPortalUrl,username,password,21600);                     
                                        if (tokenStr.Contains("Unable to generate token.")){
                                            Console.WriteLine(tokenStr);
                                            return;
                                        }                 
                                        dynamic tokenJson = JsonConvert.DeserializeObject(tokenStr); 
                                        token = tokenJson["token"];                                    
                                        metadata[1] = new Grpc.Core.Metadata.Entry("authorization", $"Bearer {token}");
                                        response = await grpcClient.sendAsync(request, metadata); 
                                        responseString = response.Message;                            
                                    }
                                }
                                else{
                                    using var call = grpcClient.stream(metadata);
                                    await call.RequestStream.WriteAsync(request);
                                    await call.RequestStream.CompleteAsync();
                                    response = await call;
                                    responseString = response.Message;                         
                                    //Console.WriteLine($"Response: {responseString}");
                                }
                                
                                
                                //countTotal += count;
                                stopwatch.Stop();
                                int elapsed_time = (int)stopwatch.ElapsedMilliseconds;                                
                                if (elapsed_time < sendInterval) {
                                    Console.WriteLine(string.Format($"A batch of {count} events has been sent in {elapsed_time}ms. Waiting for {sendInterval - elapsed_time}ms. Total sent: {countTotal}. Total elapsed time: {(int)taskStopwatch.ElapsedMilliseconds}ms"));
                                    Thread.Sleep(sendInterval - elapsed_time);
                                }
                                else
                                {
                                    Console.WriteLine(string.Format($"A batch of {count} events has been sent in {elapsed_time}ms. Total sent: {countTotal}. Total elapsed time: {(int)taskStopwatch.ElapsedMilliseconds}ms"));
                                }
                            }
                            catch (Exception ex){
                                Console.WriteLine(string.Format($"A batch of {count} events was sent, but the request failed. Total sent: {countTotal}. Total elapsed time: {(int)taskStopwatch.ElapsedMilliseconds}ms"));
                                Console.WriteLine(ex.Message);
                            }
                            finally{
                                request.Features.Clear();
                                stopwatch.Reset();
                                count = 0;
                            }                           
                        }
                    }
                    Console.WriteLine(string.Format($"Reached the end of the simulation file. Repeat is set to {repeatSimulation}"));
                    if (!repeatSimulation)
                    {
                        runTask = false;
                        taskStopwatch.Stop(); 
                        Console.WriteLine($"Total task duration: {(int)taskStopwatch.ElapsedMilliseconds}ms");
                    }
                }
                
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                Console.WriteLine(e.Data);
            }
        }

        static async Task<string> getToken(string url, string user, string pass, double expiry)
        {    
            try
            {        
                var values = new Dictionary<string, string>
                {
                    { "username", user },
                    { "password", pass },
                    { "client", "referer" },
                    { "referer", "http://localhost:8888"},
                    { "f", "json"},
                    { "expiration", expiry.ToString()}
                };
                
                var content = new FormUrlEncodedContent(values);
                var response = await httpClient.PostAsync($"{url}/sharing/rest/generateToken", content);            
                var responseString = await response.Content.ReadAsStringAsync();
                return responseString;
            }
            catch (Exception e)
            {
                Console.Out.WriteLine("getToken Error: " + e.Message);
                //log.LogInformation("Error: " + e.Message);
                return "getToken Error: " + e.Message;
            }
        }
    }
}
