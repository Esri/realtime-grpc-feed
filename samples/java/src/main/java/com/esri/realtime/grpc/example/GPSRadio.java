package com.esri.realtime.grpc.example;

import com.esri.realtime.core.grpc.Feature;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

/**
 * An example object to be wrapped by the Feature. When Features are sent to the gRPC Feed, the order of the parameters
 * must match the schema order in the Feed details page. It is a good idea to wrap your objects with a translation
 * method that converts them into Features.
 **/
public class GPSRadio
{
  protected String  deviceID;          // TRACK_ID
  protected String  alias;
  protected String  email;
  protected String  event;
  protected String  priority;
  protected Double  latitude;
  protected Double  longitude;
  protected Integer altitudeM;
  protected Float   speedKph;
  protected Integer headingDeg;
  protected Integer satellites;
  protected Float   accuracyM;
  protected Float   signalStrengthDbms;
  protected Long    fixTime;
  protected Long    eventTime;         // START_TIME
  protected Boolean isEmergency;
  protected Boolean isPoweredOn;

  /**
   * The getRandom() method simply creates a random GPS Radio object. It should be replaced by your business logic.
   * 
   * @return GPSRadio object that has been created with random data
   */
  public static GPSRadio getRandom()
  {
    GPSRadio randomRadio = new GPSRadio();
    randomRadio.deviceID = String.valueOf(Math.random() * 100); // TRACK_ID
    randomRadio.alias = "Device" + String.valueOf(randomRadio.deviceID);
    randomRadio.email = "Device" + String.valueOf(randomRadio.deviceID) + "@esri.com";
    randomRadio.event = "beacon";
    randomRadio.priority = "low";
    randomRadio.latitude = 60 - Math.random() * 120;
    randomRadio.longitude = 180 - Math.random() * 360;
    randomRadio.altitudeM = new Double(Math.random() * 10000).intValue();
    randomRadio.speedKph = new Double(Math.random() * 32).floatValue();
    randomRadio.headingDeg = new Double(Math.random() * 359).intValue();
    randomRadio.satellites = new Double(Math.random() * 16).intValue();
    randomRadio.accuracyM = new Double(Math.random() * 10).floatValue();
    randomRadio.signalStrengthDbms = new Double(-Math.random() * 100 - 60).floatValue();
    randomRadio.fixTime = System.currentTimeMillis();
    randomRadio.eventTime = System.currentTimeMillis(); // START_TIME
    randomRadio.isEmergency = false;
    randomRadio.isPoweredOn = true;

    return randomRadio;
  }

  /**
   * Translates the GPSRadio object into a gRPC Feature object. Note the schema (order the parameters are packed into
   * the feature) MUST match the schema defined on the Velocity gRPC details page.
   * 
   * @param gpsRadio
   *          a business object to translate
   * @return a Velocity gRPC Feature
   */
  public static Feature asFeature(GPSRadio gpsRadio)
  {
    Feature.Builder featureBuilder = Feature.newBuilder();

    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.deviceID)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.alias)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.email)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.event)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.priority)));

    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.latitude)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.longitude)));
    featureBuilder.addAttributes(Any.pack(Int32Value.of(gpsRadio.altitudeM)));

    featureBuilder.addAttributes(Any.pack(FloatValue.of(gpsRadio.speedKph)));
    featureBuilder.addAttributes(Any.pack(Int32Value.of(gpsRadio.headingDeg)));

    featureBuilder.addAttributes(Any.pack(Int32Value.of(gpsRadio.satellites)));
    featureBuilder.addAttributes(Any.pack(FloatValue.of(gpsRadio.accuracyM)));
    featureBuilder.addAttributes(Any.pack(FloatValue.of(gpsRadio.signalStrengthDbms)));

    featureBuilder.addAttributes(Any.pack(Int64Value.of(gpsRadio.fixTime)));
    featureBuilder.addAttributes(Any.pack(Int64Value.of(gpsRadio.eventTime)));

    featureBuilder.addAttributes(Any.pack(BoolValue.of(gpsRadio.isEmergency)));
    featureBuilder.addAttributes(Any.pack(BoolValue.of(gpsRadio.isPoweredOn)));

    return featureBuilder.build();
  }

}
