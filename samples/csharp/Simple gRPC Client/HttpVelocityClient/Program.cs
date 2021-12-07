

using System.Net.Http.Headers;

HttpClient client = new HttpClient();


string jsonDataString = "[{\"lat\":39.29242438926388,\"lon\":-76.6666720609419,\"name\":\"Evan\",\"active\":false,\"id\":4,\"timestamp\":1636384539000},{\"lat\":38.905809,\"lon\":-77.091489,\"name\":\"Brody\",\"active\":true,\"id\":1,\"timestamp\":1636384599000},{\"lat\":38.580191,\"lon\":-77.421078,\"name\":\"Sarah\",\"active\":false,\"id\":2,\"timestamp\":1636384649000},{\"lat\":39.16077658089355,\"lon\":-77.3007033603238,\"name\":\"Cortney\",\"active\":true,\"id\":3,\"timestamp\":1636384709000}]";


var content = new StringContent(jsonDataString, System.Text.Encoding.UTF8, "application/json");
content.Headers.ContentType = new MediaTypeHeaderValue("application/json");

var response = await client.PostAsync("https://us-iotqa.arcgis.com/a4iotqa/zscdue1weby6hvnu/receiver/187f780e231e4b32b7f8591efd5aa0ce", content);
var responseString = await response.Content.ReadAsStringAsync();


Console.WriteLine(responseString);
