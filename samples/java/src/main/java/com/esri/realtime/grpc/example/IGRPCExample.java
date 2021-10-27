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