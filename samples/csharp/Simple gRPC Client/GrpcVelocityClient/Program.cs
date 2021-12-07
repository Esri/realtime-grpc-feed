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

using Grpc.Net.Client;
using Esri.Realtime.Core.Grpc;
using Google.Protobuf.WellKnownTypes;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;



//Velocity gRPC feed connection values; get these from the item details page of the Velocity feed after it has been created

//gRPC endpoint URL:
string gRPC_endpoint_URL = "";

//gRPC endpoint header path	
string gRPC_endpoint_header_path = "";

//data to send
string jsonDataString = "[{\"lat\":39.29242438926388,\"lon\":-76.6666720609419,\"name\":\"Evan\",\"active\":false,\"id\":4,\"timestamp\":1636384539000},{\"lat\":38.905809,\"lon\":-77.091489,\"name\":\"Brody\",\"active\":true,\"id\":1,\"timestamp\":1636384599000},{\"lat\":38.580191,\"lon\":-77.421078,\"name\":\"Sarah\",\"active\":false,\"id\":2,\"timestamp\":1636384649000},{\"lat\":39.16077658089355,\"lon\":-77.3007033603238,\"name\":\"Cortney\",\"active\":true,\"id\":3,\"timestamp\":1636384709000}]";

dynamic jsonData = JsonConvert.DeserializeObject<JArray>(jsonDataString);

using var channel = GrpcChannel.ForAddress($"https://{gRPC_endpoint_URL}:443");
var client = new GrpcFeed.GrpcFeedClient(channel);

var metadata = new Grpc.Core.Metadata
{
    { "grpc=path", gRPC_endpoint_header_path }
};


Request request = new Request();

foreach (var person in jsonData)
{
    Feature feature = new Feature();

    foreach (var property in person)
    {        
        var propType = property.Value.Type;
        
        if (propType is JTokenType.Boolean)
            feature.Attributes.Add(Any.Pack(new BoolValue() { Value = property.Value }));
        else if (propType is JTokenType.Float)           
            feature.Attributes.Add(Any.Pack(new FloatValue() { Value = property.Value }));
        else if (propType is JTokenType.Integer)
            feature.Attributes.Add(Any.Pack(new Int64Value() { Value = property.Value }));
        else if (propType is JTokenType.String)
            feature.Attributes.Add(Any.Pack(new StringValue() { Value = property.Value }));

    }    

    request.Features.Add(feature);
}

var reply = await client.SendAsync(request, metadata);



Console.WriteLine("Response: " + reply.Message);
Console.WriteLine("Press any key to exit...");
Console.ReadKey();
