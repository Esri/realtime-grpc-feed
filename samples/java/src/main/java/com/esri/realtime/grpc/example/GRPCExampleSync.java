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

import com.esri.realtime.core.grpc.Feature;
import com.esri.realtime.core.grpc.GrpcFeedGrpc;
import com.esri.realtime.core.grpc.GrpcFeedGrpc.GrpcFeedBlockingStub;
import com.esri.realtime.core.grpc.Request;
import com.esri.realtime.core.grpc.Response;

import io.grpc.stub.MetadataUtils;

/**
 * Example that waits for the Velocity gRPC Feed to receive an event before it sends another one.
 *
 */
public class GRPCExampleSync extends GRPCExample
{

  private final GrpcFeedBlockingStub blockingStub;

  /**
   * Creates a blocking feed stub using the channel and the metadata
   */
  public GRPCExampleSync()
  {
    blockingStub = GrpcFeedGrpc.newBlockingStub(getChannel()).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(getMetadata()));
  }

  /**
   * Sends a single message and waits (blocks) for the response
   * 
   * @param gpsRadioMessage
   *          The message to send
   */
  public void sendMessage(GPSRadio gpsRadioMessage)
  {
    Request request = makeRequest(new Feature[] { GPSRadio.asFeature(gpsRadioMessage) });
    Response response = blockingStub.send(request);

    System.out.println("\tResponse: " + response.getCode() + " " + response.getMessage());
  }

  /**
   * Iterates over an array of messages and sends them one at a time.
   * 
   * @param args
   *          not used
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException
  {
    GRPCExampleSync synchronousExample = new GRPCExampleSync();

    System.out.println("Sending " + NUM_MSGS + " messages...");

    for (int msgNum = 0; msgNum < NUM_MSGS; msgNum++)
    {
      System.out.println(msgNum + ": Sending message...");

      GPSRadio gpsRadioMessage = GPSRadio.getRandom();
      synchronousExample.sendMessage(gpsRadioMessage);
    }

    System.out.println("Done Sending " + NUM_MSGS + " messages");
  }
}
