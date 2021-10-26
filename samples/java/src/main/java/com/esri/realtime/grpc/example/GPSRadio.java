package com.esri.realtime.grpc.example;

import java.util.Date;

import com.esri.realtime.core.grpc.Feature;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

/**
 * An example object to be wrapped by the Feature. When Features are sent to the gRPC Feed, the order of the parameters
 * must match the schema order in the Feed details page. It is a good idea to wrap your objects with a translation
 * method that converts them into Features.
 * 
 * 
 **/
public class GPSRadio
{

  protected Long    ReportID;
  protected String  DeviceID;          // TRACK_ID
  protected Long    Event;
  protected String  Alias;
  protected String  Email;
  protected Long    FixTime;           // START_TIME
  protected String  EventTime;
  protected Double  Latitude;
  protected Double  Longitude;
  protected Double  AltitudeM;
  protected Double  AccuracyM;
  protected Double  SpeedKph;
  protected Double  HeadingDeg;
  protected Double  SignalStrengthDbms;
  protected Boolean IsEmergency;
  protected Boolean IsPoweredOn;

  /**
   * The getRandom() method simply creates a random GPS Radio object. It should be replaced by your business logic.
   * 
   * @return GPSRadio object that has been created with random data
   */
  public static GPSRadio getRandom()
  {
    GPSRadio randomRadio = new GPSRadio();
    randomRadio.ReportID = (long) (Math.random() * 1000);
    randomRadio.DeviceID = String.valueOf(Math.random() * 100); // TRACK_ID
    randomRadio.Event = System.currentTimeMillis();
    randomRadio.Alias = "Device" + String.valueOf(randomRadio.DeviceID);
    randomRadio.Email = "Device" + String.valueOf(randomRadio.DeviceID) + "@esri.com";
    randomRadio.FixTime = System.currentTimeMillis(); // START_TIME
    randomRadio.EventTime = new Date().toString();
    randomRadio.Latitude = 60 - Math.random() * 120;
    randomRadio.Longitude = 180 - Math.random() * 360;
    randomRadio.AltitudeM = Math.random() * 10000;
    randomRadio.AccuracyM = Math.random() * 10;
    randomRadio.SpeedKph = Math.random() * 32;
    randomRadio.HeadingDeg = Math.random() * 359;
    randomRadio.SignalStrengthDbms = -Math.random() * 100 - 60;
    randomRadio.IsEmergency = false;
    randomRadio.IsPoweredOn = true;

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
    featureBuilder.addAttributes(Any.pack(Int32Value.of(gpsRadio.ReportID.intValue())));

    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.DeviceID)));

    featureBuilder.addAttributes(Any.pack(Int32Value.of(gpsRadio.Event.intValue())));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.Alias)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.Email)));
    featureBuilder.addAttributes(Any.pack(Int64Value.of(gpsRadio.FixTime)));
    featureBuilder.addAttributes(Any.pack(StringValue.of(gpsRadio.EventTime)));

    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.Latitude)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.Longitude)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.AltitudeM)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.AccuracyM)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.SpeedKph)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.HeadingDeg)));
    featureBuilder.addAttributes(Any.pack(DoubleValue.of(gpsRadio.SignalStrengthDbms)));

    featureBuilder.addAttributes(Any.pack(BoolValue.of(gpsRadio.IsEmergency)));
    featureBuilder.addAttributes(Any.pack(BoolValue.of(gpsRadio.IsPoweredOn)));

    return featureBuilder.build();
  }

}
