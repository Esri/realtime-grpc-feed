//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: velocity_grpc.proto

package com.esri.realtime.core.grpc;

@kotlin.jvm.JvmSynthetic
inline fun response(block: com.esri.realtime.core.grpc.ResponseKt.Dsl.() -> kotlin.Unit): com.esri.realtime.core.grpc.Response =
  com.esri.realtime.core.grpc.ResponseKt.Dsl._create(com.esri.realtime.core.grpc.Response.newBuilder()).apply { block() }._build()
object ResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private val _builder: com.esri.realtime.core.grpc.Response.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.esri.realtime.core.grpc.Response.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.esri.realtime.core.grpc.Response = _builder.build()

    /**
     * <code>string message = 1;</code>
     */
    var message: kotlin.String
      @JvmName("getMessage")
      get() = _builder.getMessage()
      @JvmName("setMessage")
      set(value) {
        _builder.setMessage(value)
      }
    /**
     * <code>string message = 1;</code>
     */
    fun clearMessage() {
      _builder.clearMessage()
    }

    /**
     * <code>int32 code = 2;</code>
     */
    var code: kotlin.Int
      @JvmName("getCode")
      get() = _builder.getCode()
      @JvmName("setCode")
      set(value) {
        _builder.setCode(value)
      }
    /**
     * <code>int32 code = 2;</code>
     */
    fun clearCode() {
      _builder.clearCode()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun com.esri.realtime.core.grpc.Response.copy(block: com.esri.realtime.core.grpc.ResponseKt.Dsl.() -> kotlin.Unit): com.esri.realtime.core.grpc.Response =
  com.esri.realtime.core.grpc.ResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()