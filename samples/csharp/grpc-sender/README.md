# grpc-sender
This is a C# .Net console application to send data from a delimited text file to a gRPC endpoint using unary or streaming requests. Publish this console app to your Azure portal to create a continuously running source of messages to your gRPC feed to support demos of Velocity.


## Features
* Use grpc-sender to send records from your delimited data file as event messages to a gRPC endpoint via unary or streaming requests. 
* Provides a source of messages for your Velocity demos.

## Instructions

1. Set up a feed in ArcGIS Velocity to receive the messages sent by this app.
2. Clone the repo in Visual Studio Code.
3. Update the app.config file as follows:

    -	gRPC_endpoint_URL – Paste the complete URL for the gRPC feed to which data should be sent.
    -	gRPC_endpoint_header_path – Paste the complete gRPC endpoint header path for the gRPC feed to which data should be sent.
    -	streamData – Enter true or false to indicate whether to stream the simulation data or send it in unary requests. If sending high-velocity data, set this value to “true”.
    -	authenticationArcGIS – Enter true or false to indicate if the gRPC feed is secured using ArcGIS authentication or with no security.
    -	tokenPortalUrl – Only used if authenticationArcGIS is true. The url to the portal to be used for requesting an authentication token.
    -	username – Only used if authenticationArcGIS is true. The username for requesting an authentication token.
    -	password – Only used if authenticationArcGIS is true. The password for requesting an authentication token.
    -	fileUrl – Enter the URL to the simulation delimited file containing the data to be sent between the empty quotes for the value of fileUrl. If using our sample file, set this value to “https://a4iot-test-data.s3.us-west-2.amazonaws.com/point/Charlotte_Simulations/57Buses_in_CharlotteNC.csv”.
    -	hasHeaderRow – Enter true or false to indicate whether the simulation csv file has a header row of field names. If using our sample csv file, set this value to “true”.
    -	fieldDelimiter – the delimiter between fields in the simulation file. If using our sample csv file, set this value to “,”.
    -	numLinesPerBatch – Enter the number of lines to send with each batch. The app will read this number of lines from the simulation csv file, bundle them into a batch of events and send them to the REST endpoint all at once. Then it will read the next set of lines into a batch, send them and repeat until the end of the simulation file is reached and all lines have been sent. You might set this value to be equal to the number of unique track ids in your data or use it in conjunction with the sendInterval to simply control the rate of events into your REST endpoint. If using our sample csv file, there are 57 unique track ids.
    -	sendInterval – Enter the number of milliseconds between batches sent to the REST endpoint. This time includes the time required to send a batch. Thus, if this value is set to 1000ms, and it takes 700ms to send a batch, the app will wait 300ms before sending the next batch. If it takes longer than this value to send a batch, it will not wait before sending the next batch.
    -	timeField – The zero-based index of the field in the simulation csv file containing date values. If using our sample csv file, set this value to “0”.
    -	setToCurrentTime – Enter true or false to indicate whether to update the values in the date field to the date and time the event is sent to the REST endpoint. If using our sample csv file, set this value to “true”. 
    -	dateFormat – Optional, only used if setToCurrentTime is true. In that case the date values will be formatted as strings according to this formatter. If this value is empty, date values will be epochs. Formatting string can be standard or custom. See https://docs.microsoft.com/en-us/dotnet/standard/base-types/standard-date-and-time-format-strings and https://docs.microsoft.com/en-us/dotnet/standard/base-types/custom-date-and-time-format-strings
    -	dateCulture - Optional, examples: "en-US", "es-ES", "fr-FR"; only used if setToCurrentTime is true and dateFormat is not empty. In that case date strings will be formatted according to the culture specified in this setting or the default culture if empty
    -	repeatSimulation – Enter true or false to indicate whether to repeat the simulation when the end of the file is reached.

4. Commit the changes in the app.config file
5. Deploy to Azure App Service to your Azure portal.
6. In the resulting App Service, configure the deployment source to be LocalGit.
7. Deploy to Web App.

Detailed instructions are in the "Deploy grpc-sender to Azure App Service using Visual Studio Code .docx" file in this repo.

## Requirements

* A delimited text file of events you wish to simulate. It must be hosted in a place where it is accessible by URL such as an Amazon S3 bucket. To get started you may use the sample file hosted in this repo (https://a4iot-test-data.s3.us-west-2.amazonaws.com/point/Charlotte_Simulations/57Buses_in_CharlotteNC.csv).
* Microsoft Azure account with and active subscription. (Create one for free: https://azure.microsoft.com/free/?utm_source=campaign&utm_campaign=vscode-tutorial-appservice-extension&mktingSource=vscode-tutorial-appservice-extension)
* Visual Studio Code (VS Code) installed on your local machine. (Get it here: https://code.visualstudio.com/)
* The Azure App Service extension for VS Code (install from within VS Code or get it here: https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azureappservice)
* Git installed on your local machine. (Get it here: https://git-scm.com/)


## Resources


## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2021 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt]( https://github.com/kengorton/event-hub-sender/blob/master/license.txt) file.

ArcGIS Velocity, Microsoft Azure, C#, .Net
