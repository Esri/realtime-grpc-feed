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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.esri.realtime.core.grpc.Feature;
import com.esri.realtime.core.grpc.GrpcFeedGrpc;
import com.esri.realtime.core.grpc.GrpcFeedGrpc.GrpcFeedStub;
import com.esri.realtime.core.grpc.Request;
import com.esri.realtime.core.grpc.Response;

import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

/**
 * GRPC Example: Asynchronous with no authentication
 */
public class GRPCExampleAsync extends GRPCExample
{

  private final GrpcFeedStub      asyncStub;
  private StreamObserver<Request> requestStream;
  private CountDownLatch          latch;

  /**
   * Creates the feed stub connecting to the Velocity gRPC Feed
   */
  public GRPCExampleAsync()
  {
    asyncStub = MetadataUtils.attachHeaders(GrpcFeedGrpc.newStub(getChannel()), getMetadata());
  }

  /**
   * Opens a stream if a stream is not already open and sends a single message.
   * 
   * @param gpsRadioMessage
   *          the business object (event) to send
   * @throws InterruptedException
   */
  public void sendMessage(GPSRadio gpsRadioMessage) throws InterruptedException
  {
    openStream();
    if (requestStream != null)
    {
      Request request = makeRequest(new Feature[] { GPSRadio.asFeature(gpsRadioMessage) });
      requestStream.onNext(request);
    }
    else
    {
      System.out.println("ERROR: Stream not started");
    }
  }

  /**
   * Opens the stream with a stream observer to identify the key operations on the ResponseStream. Note that if the
   * stream encounters an error it immediately shuts down.
   * 
   * @throws InterruptedException
   */
  public void openStream() throws InterruptedException
  {
    if (requestStream == null)
    {
      latch = new CountDownLatch(1);
      StreamObserver<Response> responseObserver = new StreamObserver<Response>()
        {
          @Override
          public void onNext(Response response)
          {

            if (response.getCode() != 200)
              System.out.println("\tFailure response: " + response.getCode() + " " + response.getMessage());
            else
              System.out.println("\tSuccess response: " + response.getCode() + " " + response.getMessage());
          }

          @Override
          public void onError(Throwable throwable)
          {
            System.out.println("Error occurred while streaming features" + throwable);
            latch.countDown();
          }

          @Override
          public void onCompleted()
          {
            System.out.println("stream ending now");
            latch.countDown();
          }
        };
      System.out.println("Opening message stream.");
      requestStream = asyncStub.stream(responseObserver);
    }
  }

  /**
   * Requests the request stream shutdown using the onCompleted() method and then set to null. The ResponseStream stream
   * observer will asynchronously wait for a response from the server and decrement the latch to show the stream is
   * shutdown.
   */
  public void closeStream()
  {
    if (requestStream != null)
    {
      requestStream.onCompleted();
      requestStream = null;
    }
  }

  /**
   * Sends a list of GPSRadio objects serially, one at a time, over the stream then waits for the latch to release.
   * 
   * @param args
   *          not used
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException
  {
    GRPCExampleAsync asynchronousExample = new GRPCExampleAsync();

    // Send messages over the stream
    System.out.println("Streaming " + NUM_MSGS + " messages...");
    for (int msgNum = 0; msgNum < NUM_MSGS; msgNum++)
    {
      System.out.println(msgNum + ": Streaming message...");

      GPSRadio gpsRadioMessage = GPSRadio.getRandom();
      asynchronousExample.sendMessage(gpsRadioMessage);

      // TODO: this is to limit back pressure to avoid OOMkilled. Use flow control for higher velocity.
      Thread.sleep(SLEEP_TIMEOUT / 10);
    }
    System.out.println("Done Streaming " + NUM_MSGS + " messages");

    // Close the stream
    System.out.println("Closing message stream.");
    asynchronousExample.closeStream();

    // wait for it to complete
    asynchronousExample.latch.await(5000L, TimeUnit.SECONDS);
    assert asynchronousExample.latch.getCount() == 0 : "Response was not successful!";
    System.out.println("all requests processed...");
  }

}
