// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: velocity_grpc.proto

package com.esri.realtime.core.grpc;

public final class GrpcProto {
  private GrpcProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Request_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Request_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Feature_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Feature_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Response_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Response_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023velocity_grpc.proto\032\031google/protobuf/a" +
      "ny.proto\"%\n\007Request\022\032\n\010features\030\001 \003(\0132\010." +
      "Feature\"3\n\007Feature\022(\n\nattributes\030\001 \003(\0132\024" +
      ".google.protobuf.Any\")\n\010Response\022\017\n\007mess" +
      "age\030\001 \001(\t\022\014\n\004code\030\002 \001(\0052H\n\010GrpcFeed\022\037\n\006s" +
      "tream\022\010.Request\032\t.Response(\001\022\033\n\004send\022\010.R" +
      "equest\032\t.ResponseB*\n\033com.esri.realtime.c" +
      "ore.grpcB\tGrpcProtoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.AnyProto.getDescriptor(),
        });
    internal_static_Request_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Request_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Request_descriptor,
        new java.lang.String[] { "Features", });
    internal_static_Feature_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_Feature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Feature_descriptor,
        new java.lang.String[] { "Attributes", });
    internal_static_Response_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_Response_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Response_descriptor,
        new java.lang.String[] { "Message", "Code", });
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}