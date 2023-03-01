# ArcGIS Velocity: Custom Data Ingestion with gRPC and HTTP


#### These are the tasks outlined below:

1. Create a gRPC feed in Velocity
2. Create a gRPC client in VS Code to send data to it.
3. Create an HTTP Receiver feed in Velocity.
4. Modify the VS Code client to send data to the HTTP Receiver feed.


#### Create a gRPC feed.
Use the following JSON sample to create a gRPC feed in Velocity:

```
{
	"track_id": "029c30b0-e54a-4890-a087-c64737a0c671",
	"timestamp": 1676575876589,
	"ground_speed_mps": 0.94,
	"heading_deg": 342.97,
	"vertical_speed_mps": -1.16,
	"geometry": {
		"x": -122.4359554,
		"y": 37.742691,
		"z": 88.15,
		"spatialReference": {"wkid": 4326}
  }
}
```
When configuring the feed, the geometry is from a single field named 'geometry' which contains Points with Z value and is in EsriJSON format with a spatial reference of GCS WGS 1984 (wkid 4326). Time values are epoch milliseconds in the 'Timestamp' field and the TRACK_ID field is named 'track_id'.
With the feed created and running we can create our gRPC client and provision it with the two connection parameters ```gRPC endpoint URL``` and ```gRPC endpoint header path``` for the feed. Make note of them in the Feed Details section of the feed for future use.

#### Open VS Code 
In Windows Explorer folder of your choice (best if it's empty) right-click > Open with Code. 

In VS Code select Terminal > New Terminal.

In the VS Code terminal window execute the following commands:
(copy the commands and paste them in the VS Code terminal window at the command line)
#### Create a new project:	
```
dotnet new console -o VelocityGrpcClient
```
#### Open the new project in VS Code:
```
code -r VelocityGrpcClient
```
#### Add required package references into the .csproj file:
In VS Code select Terminal > New Terminal or type ```Ctrl+Shift+` ```.

#### Add a package reference for Grpc.Net.Client:
```
dotnet add VelocityGrpcClient.csproj package Grpc.Net.Client
```
#### Add a package reference for Google.Protobuf:
```
dotnet add VelocityGrpcClient.csproj package Google.Protobuf
```
#### Add a package reference for Grpc.Tools:
```
dotnet add VelocityGrpcClient.csproj package Grpc.Tools
```
#### Add a package reference for Newtonsoft.Json:
```
dotnet add VelocityGrpcClient.csproj package Newtonsoft.Json
```


#### Add a proto folder
In VS Code's file explorer, add a new folder called 'proto'. Right-click the 'proto' folder and select ```Reveal in File Explorer```. Download the velocity_grpc.proto file from ```https://github.com/Esri/realtime-grpc-feed/blob/main/proto/velocity_grpc.proto``` into the proto folder. 

#### Reerence the velocity_grpc.proto file in the projects VelocityGrpcClient.csproj file.
In VS Code open the VelocityGrpcClient.csproj file and add the following: 
```
	<ItemGroup>
	  <Protobuf Include="proto\velocity_grpc.proto" GrpcServices="Client" />
	</ItemGroup>
```

#### Create the client code
Open the Program.cs file and add using statements, namespace and class stubs:

```
using Grpc.Core;
using Grpc.Net.Client;
using Google.Protobuf.WellKnownTypes;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Esri.Realtime.Core.Grpc;

namespace velocityGrpc{

    class velocityGrpc{	
	}
}


```

#### Add the following to class velocityGrpc:

```	
	
	    private static bool streamData = false;        
        private static GrpcFeed.GrpcFeedClient? grpcClient;
        private static AsyncClientStreamingCall<Request,Response>? grpcClientStreamingCall;
        private static Grpc.Core.Metadata? metadata;



		static void Main()
        {
            velocityGrpc prg = new velocityGrpc();
            prg.RunAsync();
        }	

		private async void RunAsync()
        {
            string gRPC_endpoint_Url = "";
            string gRPC_endpoint_header_path = "";

            using var channel = GrpcChannel.ForAddress("https://"+ gRPC_endpoint_Url + ":443");
            grpcClient = new GrpcFeed.GrpcFeedClient(channel);

            metadata = new Grpc.Core.Metadata{
                { "grpc-path", gRPC_endpoint_header_path }
            };

            if (authenticationArcGIS){             
                metadata.Add("authorization", $"Bearer {token}");                    
            }

            grpcClientStreamingCall = streamData ? grpcClient.Stream(metadata) : null;            
            
            //TODO: add logic to fetch and send data
			
			
            
            if (streamData && grpcClientStreamingCall != null){
                await grpcClientStreamingCall.RequestStream.CompleteAsync();
            }
			
            return;

        }
```

#### In the above RunAsync() method:            
- set the value of "gRPC_endpoint_Url" variable with the actual gRPC endpoint Url of the Velocity gRPC feed you created using the JSON schema above.
- set the "gRPC_endpoint_header_path" variable with the actual gRPC endpoint header path of the same Velocity gRPC feed.
- toggle line comments to comment out the 3 lines referring to authenticationArcGIS. 
		
#### Next, add a method to prepare some data to send:
```
		
        
        private async Task<JObject> getSampleData(){
            JObject trackUpdateJson = JObject.Parse(string_to_parse_goes_here);
            TimeSpan t = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            long milliSecondsSinceEpoch = (long)t.TotalMilliseconds;
            trackUpdateJson["timestamp"] = milliSecondsSinceEpoch;
            return trackUpdateJson;
        }
```

#### Add the following string to the JObject.Parse() method call above in place of 'string_to_parse_goes_here':
```
"{'track_id':'029c30b0-e54a-4890-a087-c64737a0c671','update_seq_num':62285639,'time_of_applicability':'20230216T193116.589729+00','time_of_measurement':1676575876589,'alt_meters_msl':120.44,'ecef_pos_meters_X':-2708617.47,'ecef_pos_meters_Y':-4262183.77,'ecef_pos_meters_Z':3882953.11,'ecef_vel_mps_xdot': 0.56,'ecef_vel_mps_ydot':1.39,'ecef_vel_mps_zdot': 0,'ground_speed_mps':0.94,'heading_deg':342.97,'vertical_speed_mps':-1.16,'alt_meters_agl': -24.71,'geometry':{'x':-122.4359554,'y':37.742691,'z':88.15,'spatialReference':{'wkid':4326}}}"
```

#### Next add a method to package the above json into a gRPC feature:
```
	
	
		private async Task<Request> prepareTrackUpdatesGrpcRequestAsync(JObject trackUpdateJson){

            Request request = new Request(); 
            Feature feature = new Feature();          


            feature.Attributes.Add(Any.Pack(new StringValue() { Value = (string)trackUpdateJson["track_id"]               })); 
            feature.Attributes.Add(Any.Pack(new Int64Value() { Value = (long)trackUpdateJson["timestamp"]       }));  
            feature.Attributes.Add(Any.Pack(new DoubleValue() { Value = (double)trackUpdateJson["ground_speed_mps"]       })); 
            feature.Attributes.Add(Any.Pack(new DoubleValue() { Value = (double)trackUpdateJson["heading_deg"]            })); 
            feature.Attributes.Add(Any.Pack(new DoubleValue() { Value = (double)trackUpdateJson["vertical_speed_mps"]     })); 

            string featureGeometry = JsonConvert.SerializeObject((JObject)trackUpdateJson["geometry"]);
            feature.Attributes.Add(Any.Pack(new StringValue() { Value = featureGeometry                                   }));  
            
            request.Features.Add(feature);     
            
            return request;                
        }
```
		
		
		
#### Next add a method to send the gRPC feature to the Velocity gRPC feed via the grpcClient or clientStreamingCall created previously:	
```


        private async void sendTrackUpdateViaGrpcAsync(Request request){           
            try{ 
                if (!streamData){
                    Response response = await grpcClient.SendAsync(request, metadata);
                    Console.WriteLine("Track updates: " + response.Message);
                }
                else{                    
                    await grpcClientStreamingCall.RequestStream.WriteAsync(request);
                    Console.WriteLine($"Track updates: Streamed {request.Features.Count} feature(s)");
                }                 
            }
            catch (Exception ex){
                Console.WriteLine("Track updates: " + ex.Message);
            }
        }
```		
		
#### Finally in the RunAsync method, replace '//TODO: add logic to send data' with the following:
```

            ConsoleKeyInfo cki;
            Console.WriteLine("Press the Escape (Esc) key to quit: \n");
            do{
                while (Console.KeyAvailable == false){            
                    JObject trackUpdateJson = await getSampleData();
                    Request request = await prepareTrackUpdatesGrpcRequestAsync(trackUpdateJson);
                    sendTrackUpdateViaGrpcAsync(request);
                    System.Threading.Thread.Sleep(1000); 
                }           
                cki = Console.ReadKey(true);
            }
            while (cki.Key != ConsoleKey.Escape);
```

#### Ensure a debugger is configured.
- In project files if there is no .vscode folder, in the Run menu select "Add configuration..." and choose the ".Net 5+ and .NET Core debugger". The launch.json file should open. If it doesn't, open it from the .vscode folder. 

#### In the launch.json file:
- change the "console" property to:
```
integratedTerminal
```

Make sure the fee you created in Velocity is running. Debug your code by selecting Run > Start Debugging or click F5 on your keyboard. Verify data is ingested to the gRPC feed in Velocity.

### To demo sending data to an HTTP Receiver feed:

Use the same JSON sample as before to create a new HTTP Receiver feed in Velocity. For convenience here is the JSON sample again:
```
{
	"track_id": "029c30b0-e54a-4890-a087-c64737a0c671",
	"timestamp": 1676575876589,
	"ground_speed_mps": 0.94,
	"heading_deg": 342.97,
	"vertical_speed_mps": -1.16,
	"geometry": {
		"x": -122.4359554,
		"y": 37.742691,
		"z": 88.15,
		"spatialReference": {"wkid": 4326}
  }
}
```

#### Add the following method that sends the json data in a POST request to a REST endpoint:
```
	 
        
        private async void sendTrackUpdateViaHttpAsync(JObject trackUpdate, string Http_endpoint_URL){
            
            System.Net.Http.HttpClient httpClient = new System.Net.Http.HttpClient();
            string payload = JsonConvert.SerializeObject(trackUpdate);


            var content = new StringContent(payload, System.Text.Encoding.UTF8, "application/json");
            content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue("application/json");	    
            
            if (authenticationArcGIS){
                httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);                    
            }
	    
            try
            {
                var response = await httpClient.PostAsync(Http_endpoint_URL, content);
                if (response.StatusCode != System.Net.HttpStatusCode.OK)
                    Console.WriteLine($"Error: The HTTP server returned \nstatus code {(int)response.StatusCode}: {response.ReasonPhrase}");
                else
                    Console.WriteLine($"Success: The HTTP server returned \nstatus code {(int)response.StatusCode}: {response.ReasonPhrase}");
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                Console.WriteLine(e.Data);                
            }

        }
```		
#### In RunAsync() method comment out the following lines:
		Request request = await prepareTrackUpdatesRequestAsync(trackUpdateJson);
		sendTrackUpdateViaGrpcAsync(request);
		
#### and add:
```
			    string Http_endpoint_URL = "";
		            sendTrackUpdateViaHttpAsync(trackUpdateJson, Http_endpoint_URL);

```


#### In the line above:
- provision the Http_endpoint_URL variable with the url to the HTTP Receiver feed.
		
		 
Run the code again and verify data is ingested to the HTTP Receiver feed in Velocity.

