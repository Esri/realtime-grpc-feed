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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.esri.realtime.core.grpc.Feature;
import com.esri.realtime.core.grpc.GrpcFeedGrpc;
import com.esri.realtime.core.grpc.GrpcFeedGrpc.GrpcFeedStub;
import com.esri.realtime.core.grpc.Request;
import com.esri.realtime.core.grpc.Response;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;

/**
 * TODO: IN PROGRESS, not completed yet
 *
 */
public class GRPCExampleFlowControl extends GRPCExample
{

  private List<Feature>      list = Collections.synchronizedList(new LinkedList<Feature>());
  private final GrpcFeedStub asyncStub;
  private ResponseObserver   responseObserver;

  public GRPCExampleFlowControl()
  {
    asyncStub = MetadataUtils.attachHeaders(GrpcFeedGrpc.newStub(getChannel()), getMetadata());
  }

  public void sendMessage(GPSRadio gpsRadioMessage) throws InterruptedException
  {
    openStream();

    if (responseObserver == null)
    {
      System.out.println("ERROR: Stream not started");
    }
    else
    {
      list.add(GPSRadio.asFeature(gpsRadioMessage));
      // Thread.sleep(0, 1);
    }
  }

  public void openStream() throws InterruptedException
  {
    if (responseObserver == null || responseObserver.isClosed())
    {
      responseObserver = new ResponseObserver();
      System.out.println("Opening message stream.");
      asyncStub.stream(responseObserver);
    }
  }

  public void closeStream() throws InterruptedException
  {
    if (responseObserver != null)
    {
      responseObserver.onCompleted();
      awaitClosed(5000L, TimeUnit.SECONDS);
      responseObserver = null;
    }
  }

  public boolean isClosed()
  {
    return responseObserver == null ? true : responseObserver.isClosed();
  }

  public void awaitClosed(long timeout, TimeUnit unit) throws InterruptedException
  {
    if (responseObserver != null)
      responseObserver.awaitClosed(timeout, unit);
  }

  private class ResponseObserver implements ClientResponseObserver<Request, Response>
  {
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void beforeStart(ClientCallStreamObserver<Request> clientCallStreamObserver)
    {
      clientCallStreamObserver.setOnReadyHandler(new Runnable()
        {
          @Override
          public void run()
          {
            while (clientCallStreamObserver.isReady())
            {
              List<Feature> featureList = list;
              list = new ArrayList<Feature>();
              if (featureList.size() > 0)
              {
                System.out.println("Sending " + featureList.size() + " features");
                clientCallStreamObserver.onNext(makeRequest(featureList));
              }
              else if (isClosed())
              {
                System.out.println("No features to send");
                clientCallStreamObserver.onCompleted();
              }
            }
          }
        });
    }

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
      System.out.println("error occurred while streaming features" + throwable);
      latch.countDown();
    }

    @Override
    public void onCompleted()
    {
      System.out.println("stream ending now");
      latch.countDown();
    }

    public boolean isClosed()
    {
      return latch.getCount() <= 0;
    }

    public void awaitClosed(long timeout, TimeUnit unit) throws InterruptedException
    {
      System.out.println("Waiting for latch to count down");
      latch.await(timeout, unit);
    }
  }

  public static void main(String[] args) throws InterruptedException
  {

    GRPCExampleFlowControl flowControlExample = new GRPCExampleFlowControl();

    Thread t = new Thread()
      {
        public void run()
        {
          try
          {
            // Send messages over the stream
            System.out.println("Streaming " + NUM_MSGS + " messages...");
            for (int msgNum = 0; msgNum < NUM_MSGS; msgNum++)
            {
              System.out.println(msgNum + ": Streaming message...");

              GPSRadio gpsRadioMessage = GPSRadio.getRandom();
              flowControlExample.sendMessage(gpsRadioMessage);
            }
            System.out.println("Done Streaming " + NUM_MSGS + " messages");
          }
          catch (InterruptedException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        };
      };
    t.start();

    Thread.sleep(100);

    // Close the stream
    System.out.println("Closing message stream.");
    flowControlExample.closeStream();

    // wait for it to complete
    flowControlExample.awaitClosed(5000L, TimeUnit.SECONDS);
    System.out.println("all requests processed...");

  }
}
