package com.esri.realtime.grpc.example;

public interface IGRPCExample
{
  // No protocol just the fqdn host name
  // private static final String HOST_NAME: us-iotdev.arcgis.com // From feed details
  public static final String HOST_NAME               = "a4iot-a4iotdev-c2.westus2.cloudapp.azure.com"; // From feed details
  public static final int    HOST_PORT               = 443;                                         // MUST port 443

  public static final String GRPC_PATH_HEADER_KEY    = "grpc-path";
  public static final String GRPC_PATH_HEADER_VALUE  = "a4iotdev.c27cb85dff014e40826ad61918986451"; // From feed details

  public static final String GRPC_TOKEN_HEADER_KEY   = "authorization";
  public static final String GRPC_TOKEN_HEADER_VALUE = null;                                        // "Bearer <token>"
  // public static final String GRPC_TOKEN_HEADER_VALUE = "Bearer <token>"

  // Authentication settings
  public static final int    TOKEN_MAX_AGE           = 120;                                         // Max 15000 min
  public static final int    TOKEN_REFRESH           = TOKEN_MAX_AGE / 2;                           // minutes
  public static final String TOKEN_USERNAME          = "";
  public static final String TOKEN_PASSWORD          = ""; 

  // Run parameters for the test
  public static final int    NUM_MSGS                = 100;
  public static final long   SLEEP_TIMEOUT           = 500;

}
