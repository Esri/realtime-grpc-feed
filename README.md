# velocity-grpc-client

This repo contains resources for developers interested in creating clients to gRPC feeds in ArcGIS Velocity. Click here for more information on [ArcGIS Velocity]( https://www.esri.com/en-us/arcgis/products/arcgis-velocity/resources). In order to leverage gRPC feeds in Velocity you must first create the feed which will provide values required when developing the client.


## Features
* precompiled client libraries - The `libs` folder contains precompiled libraries generated from the velocity_grpc.proto file compiled into each language using the protocol buffer compiler. Download the libraries for the language of your client and reference them in your code. They will provide the necessary classes to enable the client to communicate with a Velocity gRPC feed service. We created these to remove the requirement for most developers to install nd use the protocol buffer compiler in their own environments.
* Reference implementations - The `samples` folder contains example reference implementations in various languages that illustrate how to communicate with Velocity gRPC feed services.
* velocity_grpc.proto - This file is used by developers who do not wish to use the precompiled libraries available in this repo. Use this file with the protocol buffer compiler to generate the necessary client libraries. Click here for more information on [protocol buffers and the use of the protocol buffer compiler]( https://developers.google.com/protocol-buffers).

## Instructions

1. Download the precompiled libraries for the language of your gRPC client. 
2. Reference them appropriately in your client codebase.
3. Make use of the reference imlementations available in this repo for guidance when creating your own clients.
4. Alternatively, instead of using the provided precompiled libraries, create your own by downloading the `velocity_grpc.proto` file and compilit it using the protocol buffer compiler. Use the resulting client libraries in your client codebase.

## Requirements

* IDE appropriate for your development scenario

## Resources

* [ArcGIS Velocity](https://www.esri.com/en-us/arcgis/products/arcgis-velocity/resources)
* [ArcGIS Velocity gRPC feeds](https://doc.arcgis.com/en/iot/ingest/grpc.htm)
* [gRPC](https://grpc.io/)
* [Protocol Buffers](https://developers.google.com/protocol-buffers)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2016 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt]( https://raw.github.com/Esri/quickstart-map-js/master/license.txt) file.

[](Esri Tags: ArcGIS Web Mapping QuickStart)
[](Esri Language: JavaScript)​
