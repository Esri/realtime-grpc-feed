# velocity-grpc-java-examples
ArcGIS Velocity gRPC Feed Examples in Java

These examples show how to construct Java clients for ArcGIS Velocity gRPC feeds.

Additional documentation can be found [here](https://github.com/Esri/realtime-grpc-feed/blob/main/doc/ArcGIS%20Velocity%20gRPC%20Client%20Developer%20Guide.pdf).

# Configuring the Sample
1. Update the IGRPCExample.java interface with the following parameters from the Velocity gPRC Feed Item Details page:<br>
    a. your **HOST_NAME** providing the fully qualified domain name to your feed server<br>
    b. and **GRPC_PATH_HEADER_VALUE** providing the item id<br>
2. If your feed is configured to use ArcGIS Authentication update one of the following in the IGRPCExample.java interface:<br>
    a. A **TOKEN_USERNAME** and **TOKEN_PASSWORD** of an ArcGIS Velocity user if your feed is configured to use ArcGIS Authentication<br>
    b. A hardcoded **GRPC_TOKEN_HEADER_VALUE** in the format "Bearer <token>" for limited testing (tokens expire quickly)<br>
3. Use Java to run either the **_GRPCExampleSync.java_** or **_GRPCExampleAsync.java_**<br>
    a. In a command line window, change directory into your cloned repository and navigate down to the java source files<br>
    b. java GRPCExampleSync.java<br>


# Building
Open a command prompt on your machine and verify you have Java and Maven installed on your machine
1. javac -version
2. mvn -version

Clone the repository to your local machine

3. git clone repository-url

Build the cloned repository

4. cd repository-name
5. mvn clean install
  
# Running the examples
Each example contains a main() method to test the core logic.
