package com.esri.realtime.core.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.41.0)",
    comments = "Source: velocity_grpc.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GrpcFeedGrpc {

  private GrpcFeedGrpc() {}

  public static final String SERVICE_NAME = "GrpcFeed";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request,
      com.esri.realtime.core.grpc.Response> getStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "stream",
      requestType = com.esri.realtime.core.grpc.Request.class,
      responseType = com.esri.realtime.core.grpc.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request,
      com.esri.realtime.core.grpc.Response> getStreamMethod() {
    io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request, com.esri.realtime.core.grpc.Response> getStreamMethod;
    if ((getStreamMethod = GrpcFeedGrpc.getStreamMethod) == null) {
      synchronized (GrpcFeedGrpc.class) {
        if ((getStreamMethod = GrpcFeedGrpc.getStreamMethod) == null) {
          GrpcFeedGrpc.getStreamMethod = getStreamMethod =
              io.grpc.MethodDescriptor.<com.esri.realtime.core.grpc.Request, com.esri.realtime.core.grpc.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "stream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.esri.realtime.core.grpc.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.esri.realtime.core.grpc.Response.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcFeedMethodDescriptorSupplier("stream"))
              .build();
        }
      }
    }
    return getStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request,
      com.esri.realtime.core.grpc.Response> getSendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "send",
      requestType = com.esri.realtime.core.grpc.Request.class,
      responseType = com.esri.realtime.core.grpc.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request,
      com.esri.realtime.core.grpc.Response> getSendMethod() {
    io.grpc.MethodDescriptor<com.esri.realtime.core.grpc.Request, com.esri.realtime.core.grpc.Response> getSendMethod;
    if ((getSendMethod = GrpcFeedGrpc.getSendMethod) == null) {
      synchronized (GrpcFeedGrpc.class) {
        if ((getSendMethod = GrpcFeedGrpc.getSendMethod) == null) {
          GrpcFeedGrpc.getSendMethod = getSendMethod =
              io.grpc.MethodDescriptor.<com.esri.realtime.core.grpc.Request, com.esri.realtime.core.grpc.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "send"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.esri.realtime.core.grpc.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.esri.realtime.core.grpc.Response.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcFeedMethodDescriptorSupplier("send"))
              .build();
        }
      }
    }
    return getSendMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GrpcFeedStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GrpcFeedStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GrpcFeedStub>() {
        @java.lang.Override
        public GrpcFeedStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GrpcFeedStub(channel, callOptions);
        }
      };
    return GrpcFeedStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GrpcFeedBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GrpcFeedBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GrpcFeedBlockingStub>() {
        @java.lang.Override
        public GrpcFeedBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GrpcFeedBlockingStub(channel, callOptions);
        }
      };
    return GrpcFeedBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GrpcFeedFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GrpcFeedFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GrpcFeedFutureStub>() {
        @java.lang.Override
        public GrpcFeedFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GrpcFeedFutureStub(channel, callOptions);
        }
      };
    return GrpcFeedFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class GrpcFeedImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * client streaming rpc for high velocity
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Request> stream(
        io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     * simple rpc for lower velocity
     * </pre>
     */
    public void send(com.esri.realtime.core.grpc.Request request,
        io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getStreamMethod(),
            io.grpc.stub.ServerCalls.asyncClientStreamingCall(
              new MethodHandlers<
                com.esri.realtime.core.grpc.Request,
                com.esri.realtime.core.grpc.Response>(
                  this, METHODID_STREAM)))
          .addMethod(
            getSendMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.esri.realtime.core.grpc.Request,
                com.esri.realtime.core.grpc.Response>(
                  this, METHODID_SEND)))
          .build();
    }
  }

  /**
   */
  public static final class GrpcFeedStub extends io.grpc.stub.AbstractAsyncStub<GrpcFeedStub> {
    private GrpcFeedStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcFeedStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GrpcFeedStub(channel, callOptions);
    }

    /**
     * <pre>
     * client streaming rpc for high velocity
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Request> stream(
        io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * simple rpc for lower velocity
     * </pre>
     */
    public void send(com.esri.realtime.core.grpc.Request request,
        io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class GrpcFeedBlockingStub extends io.grpc.stub.AbstractBlockingStub<GrpcFeedBlockingStub> {
    private GrpcFeedBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcFeedBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GrpcFeedBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * simple rpc for lower velocity
     * </pre>
     */
    public com.esri.realtime.core.grpc.Response send(com.esri.realtime.core.grpc.Request request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class GrpcFeedFutureStub extends io.grpc.stub.AbstractFutureStub<GrpcFeedFutureStub> {
    private GrpcFeedFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcFeedFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GrpcFeedFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * simple rpc for lower velocity
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.esri.realtime.core.grpc.Response> send(
        com.esri.realtime.core.grpc.Request request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND = 0;
  private static final int METHODID_STREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GrpcFeedImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GrpcFeedImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND:
          serviceImpl.send((com.esri.realtime.core.grpc.Request) request,
              (io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.stream(
              (io.grpc.stub.StreamObserver<com.esri.realtime.core.grpc.Response>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GrpcFeedBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GrpcFeedBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.esri.realtime.core.grpc.GrpcProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GrpcFeed");
    }
  }

  private static final class GrpcFeedFileDescriptorSupplier
      extends GrpcFeedBaseDescriptorSupplier {
    GrpcFeedFileDescriptorSupplier() {}
  }

  private static final class GrpcFeedMethodDescriptorSupplier
      extends GrpcFeedBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GrpcFeedMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GrpcFeedGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GrpcFeedFileDescriptorSupplier())
              .addMethod(getStreamMethod())
              .addMethod(getSendMethod())
              .build();
        }
      }
    }
    return result;
  }
}
