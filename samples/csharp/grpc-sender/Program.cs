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
using System.Data;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Grpc.Net.Client;
using Google.Protobuf.WellKnownTypes;
using Esri.Realtime.Core.Grpc;

namespace gRPC_Sender
{
    class Program
    {
        
        private static string gRPC_endpoint_URL = ConfigurationManager.AppSettings["gRPC_endpoint_URL"];
        private static string gRPC_endpoint_header_path = ConfigurationManager.AppSettings["gRPC_endpoint_header_path"];
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
        private static long iterationLimit = Int64.Parse(ConfigurationManager.AppSettings["iterationLimit"]);
        private static long realRateMultiplier = Int64.Parse(ConfigurationManager.AppSettings["realRateMultiplier"]);
        private static int timeField = Int32.Parse(ConfigurationManager.AppSettings["timeField"]);
        private static bool setToCurrentTime = Boolean.Parse(ConfigurationManager.AppSettings["setToCurrentTime"]);
        private static string dateFormat = ConfigurationManager.AppSettings["dateFormat"];
        private static CultureInfo dateCulture = CultureInfo.CreateSpecificCulture(ConfigurationManager.AppSettings["dateCulture"]);
        private static int tokenExpiry = Int32.Parse(ConfigurationManager.AppSettings["tokenExpiry"]); 
                
        static async Task Main()
        {           
            //ReadCsvFile();
            //return;

            Console.WriteLine(gRPC_endpoint_URL);
            Console.WriteLine(gRPC_endpoint_header_path);
            Console.WriteLine(streamData);
            Console.WriteLine(authenticationArcGIS);
            Console.WriteLine(fileUrl);
            Console.WriteLine(iterationLimit);

            Grpc.Core.AsyncClientStreamingCall<Request, Response> call = null;
            Request request = new Request();            
            Response response = new Response();


            using var channel = GrpcChannel.ForAddress($"https://{gRPC_endpoint_URL}:443");
            var grpcClient = new GrpcFeed.GrpcFeedClient(channel); 

            var metadata = new Grpc.Core.Metadata
            {
                { "grpc-path", gRPC_endpoint_header_path }
            };

            string token = await getTokenAsync(tokenPortalUrl,username,password);                     
            if (authenticationArcGIS){
                if (token == "")
                    return;
                metadata.Add("authorization", $"Bearer {token}");                    
            }  

            if (streamData){
                call = grpcClient.Stream(metadata);
            }
            string feedId = gRPC_endpoint_header_path.Split(".").Last();
            string velocityApiUrl = await getVelocityApiEndpointAsync(tokenPortalUrl, token: token, username: "", password:"");
            JObject feedSchema = await getFeedSchemaAsync(velocityApiUrl,feedId,token);
            
            numLinesPerBatch = sendInterval == -1 ? 1 : numLinesPerBatch;
            int featuresInBatchCount = 0;
            int totalFeaturesSentCount = 0;
            
            double maxIterations =  double.PositiveInfinity;        //iterationLimit;    
            if(iterationLimit < 1) 
                maxIterations = double.PositiveInfinity;

            int iterationCount = 0;            
            DateTime batchStartTime = DateTime.MinValue;


            DateTime previousLineDateTime = DateTime.MinValue;
            DateTime currentLineDateTime = DateTime.MinValue;
            bool hasCurrentDate = false;
            long waitTime = -1;
            string timeString;
            long timeUnix;


            try
            {
                
                string[] contentArray = await readFile(fileUrl);
                
                JArray fieldArray = (JArray)feedSchema["schema"];
                
                
                if (hasHeaderRow){
                    contentArray = contentArray.Where((source, index) => index != 0).ToArray();
                }
                int lineCount = contentArray.Length;
                                
                while (iterationCount < maxIterations)
                {
                    foreach (string line in contentArray)
                    { 
                        if (request.Features.Count == 0)  
                            batchStartTime = DateTime.UtcNow;
                        string[] values = Regex.Split(line, $"{fieldDelimiter}(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                        
                        //replace the time value of the line with the current time
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



                        //create the gRPC feature and add attributes from the line
                        Feature feature = new Feature();

                        bool boolVal = false;
                        float floatVal = 0;
                        double doubleVal = 0;
                        Int32 intVal = 0;
                        long longVal = 0;

                        for (var v = 0; v < values.Length; v++)
                        {
                            string value = values[v];
                            var fieldType = (string)fieldArray[v]["dataType"];

                            if (fieldType == "Boolean" & bool.TryParse(value, out boolVal))
                            {
                                feature.Attributes.Add(Any.Pack(new BoolValue() { Value = boolVal }));
                            }
                            else if (fieldType == "Date")
                            {
                                if (long.TryParse(value, out longVal))
                                {
                                    feature.Attributes.Add(Any.Pack(new Int64Value() { Value = longVal }));
                                }
                                else
                                {
                                    feature.Attributes.Add(Any.Pack(new StringValue() { Value = value }));
                                }

                            }
                            else if (fieldType == "Float32" & float.TryParse(value, out floatVal))
                            {
                                feature.Attributes.Add(Any.Pack(new FloatValue() { Value = floatVal }));
                            }
                            else if (fieldType == "Float64" & double.TryParse(value, out doubleVal))
                            {
                                feature.Attributes.Add(Any.Pack(new DoubleValue() { Value = doubleVal }));
                            }
                            else if (fieldType == "Int32" & int.TryParse(value, out intVal))
                            {
                                feature.Attributes.Add(Any.Pack(new Int32Value() { Value = intVal }));
                            }
                            else if (fieldType == "Int64" & long.TryParse(value, out longVal))
                            {
                                feature.Attributes.Add(Any.Pack(new Int64Value() { Value = longVal }));
                            }
                            else if (fieldType == "String")
                            {
                                feature.Attributes.Add(Any.Pack(new StringValue() { Value = value }));
                            }

                        }  

                        request.Features.Add(feature);                  
                        
                        featuresInBatchCount++;
                        totalFeaturesSentCount++;
                        

                        //sending or streaming the gRPC Request
                        if (featuresInBatchCount == numLinesPerBatch || totalFeaturesSentCount == lineCount)
                        {                           
                            // send the batch of events to the gRPC receiver                           
                            //if the request fails because the token expired, get a new one and retry the request
                            
                            try{

                                if (!streamData){                                    
                                    response = await grpcClient.SendAsync(request, metadata);
                                }
                                else{                                   
                                    await call.RequestStream.WriteAsync(request);
                                }                                 
                                Console.WriteLine($"A gRPC Request containing {numLinesPerBatch} feature has been sent. Total sent: {totalFeaturesSentCount}.\n" );
                            }
                            catch(Grpc.Core.RpcException rpcEx){
                                 if (rpcEx.StatusCode == Grpc.Core.StatusCode.PermissionDenied && authenticationArcGIS){ 
                                    token = await getTokenAsync(tokenPortalUrl,username,password);                     
                                    if (token == "")
                                        return;                              
                                    metadata[1] = new Grpc.Core.Metadata.Entry("authorization", $"Bearer {token}");
                                    response = await grpcClient.SendAsync(request, metadata);      
                                 }
                            }
                            catch (Exception e){
                                Console.WriteLine(e.Message);
                                Console.WriteLine(e.StackTrace);
                                Console.WriteLine(e.Data);
                            }
                            finally{
                                //implement the configured time delay
                                //for real rate (sendInterval == -1) it will use the values in the timeField and wait the same
                                //amount of time (or a proportion thereof depending on the value of realRateMultiplier)
                                //otherwise wait the duration of sendInterval minus the time that has already elapsed
                                if (sendInterval == -1)
                                {
                                    timeString = values[timeField];
                                    hasCurrentDate = false;

                                    if (long.TryParse(timeString, out timeUnix))
                                    {
                                        currentLineDateTime = timeString.Length == 10 ? DateTimeOffset.FromUnixTimeSeconds(timeUnix).DateTime : DateTimeOffset.FromUnixTimeMilliseconds(timeUnix).DateTime;
                                        hasCurrentDate = true;
                                    }
                                    else
                                    {
                                        hasCurrentDate = DateTime.TryParse(timeString, out currentLineDateTime);
                                    }

                                    if (hasCurrentDate && previousLineDateTime != DateTime.MinValue && currentLineDateTime > previousLineDateTime)
                                    {
                                        waitTime = Convert.ToInt64((currentLineDateTime - previousLineDateTime).TotalMilliseconds);
                                        Thread.Sleep((int)(waitTime / (realRateMultiplier / 100)));
                                    }
                                    if (hasCurrentDate)
                                    {
                                        previousLineDateTime = currentLineDateTime;
                                    }
                                }
                                else{
                                    long elapsedTime = Convert.ToInt64((DateTime.UtcNow - batchStartTime).TotalMilliseconds);   
                                    if (elapsedTime < sendInterval){                               
                                        Thread.Sleep((int)(sendInterval - elapsedTime));
                                    }
                                }
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

        static async Task<string[]> readFile(string fileUrl){

            Console.WriteLine($"Fetching and reading file: {fileUrl}");
            HttpWebRequest myHttpWebRequest = (HttpWebRequest)WebRequest.Create(fileUrl);
            HttpWebResponse myHttpWebResponse = (HttpWebResponse)myHttpWebRequest.GetResponse();
            Stream receiveStream = myHttpWebResponse.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8"); 
            StreamReader readStream = new StreamReader(receiveStream, encode);

            
            // Read lines from the file until the end of 
            // the file is reached.
            string[] contentArray = readStream.ReadToEnd().Replace("\r", "").Split('\n');

            readStream.Close();

            return contentArray;

        }

        static async Task<string> getTokenAsync(string url, string user, string pass)
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
    
         static async Task<JObject> getFeedSchemaAsync(string velocityUrl, string  feedId,string userToken)
        {

            
            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Accept", "*/*");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Referer", "http://localhost:8888");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Authorization", $"token={userToken}");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Content-Type", "application/json; charset=utf-8");

            try
            {

                string reqUrl = $"{velocityUrl}/iot/feed/{feedId}?f=json&token={userToken}";
                var response = httpClient.GetAsync(reqUrl).Result;
                var responseString = await response.Content.ReadAsStringAsync();
                dynamic feedJson = JsonConvert.DeserializeObject(responseString);
                
                string feedDefName = (string)feedJson["feed"]["name"];                    
                //{
                string label = (string)feedJson["label"];
                JToken schema = (JToken)feedJson["feed"]["schemaTransformation"]["inputSchema"]["attributes"];

                JObject feedValue = new JObject();
                feedValue.Add("label", label);
                feedValue.Add("itemId", (string)feedJson["id"]);
                JToken propBag = feedJson["feed"]["properties"];

                if (feedDefName == "azure-event-hub" || feedDefName == "azure-service-bus")
                {
                    feedValue.Add("endpoint", (string)propBag[$"{feedDefName}.endpoint"]);
                    string entityPath = feedDefName == "azure-event-hub" ? (string)propBag[$"{feedDefName}.entityPath"] : (string)propBag[$"{feedDefName}.topicName"];
                    feedValue.Add("entityPath", entityPath);
                    feedValue.Add("sharedAccessKeyName", (string)propBag[$"{feedDefName}.sharedAccessKeyName"]);
                    feedValue.Add("format", (string)feedJson["feed"]["formatName"]);
                }
                
                else if (feedDefName == "kinetic" || feedDefName == "mqtt")
                {
                    feedValue.Add("host", (string)propBag[$"{feedDefName}.host"]);
                    feedValue.Add("clientid", (string)propBag[$"{feedDefName}.clientid"]);
                    feedValue.Add("qos", (string)propBag[$"{feedDefName}.qos"]);
                    feedValue.Add("port", (string)propBag[$"{feedDefName}.port"]);
                    feedValue.Add("topic", (string)propBag[$"{feedDefName}.topic"]);
                    feedValue.Add("username", (string)propBag[$"{feedDefName}.username"]);
                    feedValue.Add("format", (string)feedJson["feed"]["formatName"]);
                }
                else if (feedDefName == "grpc")
                {
                    feedValue.Add("url", (string)propBag["grpc.url"]);
                    feedValue.Add("headerPath", (string)propBag["grpc.headerPath"]);
                    feedValue.Add("authType", (string)propBag["grpc.authenticationType"]);
                }
                else if (feedDefName == "http-receiver")
                {
                    feedValue.Add("url", (string)propBag["http-receiver.url"]);
                    feedValue.Add("authType", (string)propBag["http-receiver.httpAuthenticationType"]);
                    feedValue.Add("format", (string)feedJson["feed"]["formatName"]);
                }
                feedValue.Add("schema", schema);


                return feedValue;
            }
            catch (Exception e)
            {
                Console.Out.WriteLine("getToken Error: " + e.Message);
                return null;
            }

        }
    
        static async Task<string> getVelocityApiEndpointAsync(string tokenPortalUrl, string token, string username, string password)
        {
            if (string.IsNullOrWhiteSpace(token)){
                if ((string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))){                
                    return "Could not obtain the Velocity API url.";
                }
                token = await getTokenAsync(tokenPortalUrl, username, password);
            }

            Console.WriteLine("Fetching subscription info");

            
            HttpClient httpClient = new HttpClient();
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Accept", "application/json");
            httpClient.DefaultRequestHeaders.TryAddWithoutValidation("Content-Type", "application/json; charset=utf-8");

            try
            {
                string reqUrl = $"{tokenPortalUrl}/sharing/rest/portals/self/subscriptionInfo?f=json&token={token}&client=referer&referer=http://localhost:8888";
                var response = httpClient.GetAsync(reqUrl).Result;
                string responseString = response.Content.ReadAsStringAsync().Result;
                dynamic subscriptionInfo = JsonConvert.DeserializeObject(responseString);
                JObject error = subscriptionInfo["error"];
                if (error == null)
                {
                    JArray orgCapabilities = subscriptionInfo["orgCapabilities"];
                    foreach (JObject orgCapability in orgCapabilities)
                    {
                        if ((string)orgCapability["id"] == "velocity")
                        {
                            return (string)orgCapability["velocityUrl"];
                            //break;
                        }
                    }
                }
                //return "https://us-iotdev.arcgis.com/a4iotdev/cqvgkj9zrnkn9bcu";
                return "There was an error retrieving your organization capabilities. Ensure your organization is licensed for Velocity.";
            }
            catch (Exception e)
            {
                Console.Out.WriteLine("getToken Error: " + e.Message);
                return e.Message;
            }

        }
    } 
}
