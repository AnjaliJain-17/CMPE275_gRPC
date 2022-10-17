// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: route.proto

package route;

public interface RouteOrBuilder extends
    // @@protoc_insertion_point(interface_extends:route.Route)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 id = 1;</code>
   * @return The id.
   */
  long getId();

  /**
   * <code>int64 origin = 2;</code>
   * @return The origin.
   */
  long getOrigin();

  /**
   * <code>int64 destination = 3;</code>
   * @return The destination.
   */
  long getDestination();

  /**
   * <code>string path = 4;</code>
   * @return The path.
   */
  java.lang.String getPath();
  /**
   * <code>string path = 4;</code>
   * @return The bytes for path.
   */
  com.google.protobuf.ByteString
      getPathBytes();

  /**
   * <code>bytes payload = 5;</code>
   * @return The payload.
   */
  com.google.protobuf.ByteString getPayload();

  /**
   * <code>int64 canAcceptMore = 6;</code>
   * @return The canAcceptMore.
   */
  long getCanAcceptMore();
}
