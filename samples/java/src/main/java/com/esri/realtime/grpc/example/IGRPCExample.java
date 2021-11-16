package com.esri.realtime.grpc.example;

public interface IGRPCExample
{
  // No protocol just the fqdn host name from Feed Details page in Velocity
  public static final String HOST_NAME               = "a4iot-subscription-c2.westus2.cloudapp.azure.com";
  public static final int    HOST_PORT               = 443;                                         // MUST port 443

  // Header parameters from Feed Details page in Velocity
  public static final String GRPC_PATH_HEADER_KEY    = "grpc-path";
  public static final String GRPC_PATH_HEADER_VALUE  = "a4iot.itemnumber";

  // Token value should be of the format "Bearer <token>"
  public static final String GRPC_TOKEN_HEADER_KEY   = "authorization";
  public static final String GRPC_TOKEN_HEADER_VALUE = null;

  // Authentication settings
  public static final int    TOKEN_MAX_AGE           = 120;                                         // Max 15000 min
  public static final int    TOKEN_REFRESH           = TOKEN_MAX_AGE / 2;                           // minutes
  // Credentials should be stored in a more secure location.
  public static final String TOKEN_USERNAME          = "";
  public static final String TOKEN_PASSWORD          = ""; 

  // Run parameters for the test
  public static final int    NUM_MSGS                = 200;
  public static final int    NUM_REQUEST_FEATURES    = 20;
  public static final long   SLEEP_TIMEOUT           = 1000;

}
