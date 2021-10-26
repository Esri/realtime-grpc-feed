package com.esri.realtime.grpc.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import com.esri.realtime.core.grpc.Feature;
import com.esri.realtime.core.grpc.Request;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

/**
 * Base class that implements shared components of a gRPC client: Channel, Metadata, Request, and the ArcGIS Token
 *
 */
public class GRPCExample implements IGRPCExample
{

  public ManagedChannel            channel;
  public Metadata                  metadata;

  private String                   token;
  private ScheduledExecutorService tokenExpiration;
  private ExpireToken              expireTokenThread;

  /**
   * The Channel object represents the connection to the Velocity gRPC Feed Server.
   * 
   * @return A channel object pointing to the host and port
   */
  protected ManagedChannel getChannel()
  {
    if (channel == null)
    {
      channel = NettyChannelBuilder.forAddress(HOST_NAME, HOST_PORT).useTransportSecurity().build();
    }
    return channel;
  }

  /**
   * Metadata adds additional information to the channel that allows the Velocity host to route requests to the
   * specified feed. Optionally, the metadata object also contains the ArcGIS token used for feed level security.
   * 
   * @return The metadata with feed path and token (if specified)
   */
  protected Metadata getMetadata()
  {
    if (metadata == null)
    {
      metadata = new Metadata();

      Metadata.Key<String> grpcPathMetadataKey = Metadata.Key.of(GRPC_PATH_HEADER_KEY, Metadata.ASCII_STRING_MARSHALLER);
      metadata.put(grpcPathMetadataKey, GRPC_PATH_HEADER_VALUE);

      if (getToken() != null)
      {
        Metadata.Key<String> grpcTokenMetadataKey = Metadata.Key.of(GRPC_TOKEN_HEADER_KEY, Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(grpcTokenMetadataKey, getToken());
      }
    }

    return metadata;
  }

  /**
   * Creates a Velocity gRPC request that wraps the provided feature array.
   * 
   * @param featureArray
   *          an array of features (aka events) to pass to the Velocity gRPC Feed
   * @return The request to send via the appropriate method (send() or stream())
   */
  protected Request makeRequest(Feature[] featureArray)
  {
    final Request.Builder requestBuilder = Request.newBuilder();

    int numOfFeatures = featureArray.length;
    for (int i = 0; i < numOfFeatures; i++)
    {
      requestBuilder.addFeatures(featureArray[i]);
    }

    return requestBuilder.build();
  }

  /**
   * Creates a Velocity gRPC request that wraps the provided feature.
   * 
   * @param feature
   *          an feature (aka event) to pass to the Velocity gRPC Feed
   * @return The request to send via the appropriate method (send() or stream())
   */
  protected Request makeRequest(Feature feature)
  {
    final Request.Builder requestBuilder = Request.newBuilder();

    requestBuilder.addFeatures(feature);

    return requestBuilder.build();
  }

  /**
   * Creates a Velocity gRPC request that wraps the provided feature list.
   * 
   * @param featureList
   *          a list of features (aka events) to pass to the Velocity gRPC Feed
   * @return The request to send via the appropriate method (send() or stream())
   */
  protected Request makeRequest(List<Feature> featureList)
  {
    final Request.Builder requestBuilder = Request.newBuilder();

    featureList.forEach(feature ->
      {
        requestBuilder.addFeatures(feature);
      });

    return requestBuilder.build();
  }

  /**
   * Uses the credentials provided in the gRPC Example interface to retrieve an access token from the ArcGIS Online
   * organization. The token's age is also maintained in the interface.
   * 
   * Any application using this token should take note of the age and refresh the token before it expires.
   * 
   * Credentials should be stored in a more secure location.
   * 
   * @return A token string to be embedded into a metadata object
   */
  protected String getToken()
  {
    setupTokenExpiration();

    if (token == null)
    {
      try (final CloseableHttpClient httpclient = HttpClients.createDefault())
      {
        final HttpPost httppost = new HttpPost("https://devext.arcgis.com/sharing/rest/generateToken");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", TOKEN_USERNAME));
        nvps.add(new BasicNameValuePair("password", TOKEN_PASSWORD));
        nvps.add(new BasicNameValuePair("client", "ip"));
        nvps.add(new BasicNameValuePair("ip", getPublicIP()));
        nvps.add(new BasicNameValuePair("expiration", String.valueOf(TOKEN_MAX_AGE)));
        nvps.add(new BasicNameValuePair("f", "json"));
        httppost.setEntity(new UrlEncodedFormEntity(nvps));

        System.out.println("Executing request " + httppost.getMethod() + " " + httppost.getUri());

        // Create a custom response handler
        final HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>()
          {

            @Override
            public String handleResponse(final ClassicHttpResponse response) throws IOException
            {
              final int status = response.getCode();
              if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION)
              {
                final HttpEntity entity = response.getEntity();
                try
                {
                  return entity != null ? EntityUtils.toString(entity) : null;
                }
                catch (final ParseException ex)
                {
                  throw new ClientProtocolException(ex);
                }
              }
              else
              {
                throw new ClientProtocolException("Unexpected response status: " + status);
              }
            }

          };
        final String responseBody = httpclient.execute(httppost, responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);

        if (responseBody != null && !responseBody.trim().isEmpty())
        {
          String[] splitBody = responseBody.split("\"");
          if (splitBody.length > 4)
          {
            if (splitBody[3] != null && !splitBody[3].trim().isEmpty())
            {
              token = "Bearer " + splitBody[3];
              System.out.println(token);
            }
          }
        }
      }
      catch (Exception e)
      {
        System.out.println("Error retrieving token: " + e);
      }
    }
    return token;
  }

  /**
   * Assists the token creation by determining the public IP of the machine running the gRPC Example client.
   * 
   * @return the public IP address of this machine as a string
   */
  private String getPublicIP()
  {
    String systemipaddress = "";
    try
    {
      URL url_name = new URL("http://bot.whatismyipaddress.com");

      BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

      // reads system IPAddress
      systemipaddress = sc.readLine().trim();
    }
    catch (Exception e)
    {
      systemipaddress = "Cannot Execute Properly";
    }
    System.out.println("Public IP Address: " + systemipaddress + "\n");
    return systemipaddress;
  }

  /**
   * Example of how the token might be refreshed. A scheduled thread pool is used to replace the token every so often.
   */
  private void setupTokenExpiration()
  {
    if (tokenExpiration == null || tokenExpiration.isShutdown() || tokenExpiration.isShutdown())
    {
      tokenExpiration = Executors.newScheduledThreadPool(1);
      expireTokenThread = new ExpireToken();
      tokenExpiration.scheduleAtFixedRate(expireTokenThread, 0, TOKEN_REFRESH, TimeUnit.MINUTES);
      try
      { // give the scheduled thread pool a chance to startup
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {// pass
      }
    }
  }

  /**
   * Sets the current token to null, so a new token will be generated.
   *
   */
  private class ExpireToken implements Runnable
  {

    @Override
    public void run()
    {
      token = null;
    }

  }

  /**
   * Tests the token generation
   * 
   * @param args
   *          not used
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException
  {
    GRPCExample example = new GRPCExample();
    for (int i = 0; i < 10; i++)
    {
      example.getToken();
      Thread.sleep(TOKEN_REFRESH * 60000);// minutes -> ms
    }

  }
}
