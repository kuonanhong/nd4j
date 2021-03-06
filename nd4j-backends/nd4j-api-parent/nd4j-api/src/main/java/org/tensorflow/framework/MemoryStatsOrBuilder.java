// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tensorflow/core/framework/step_stats.proto

package org.tensorflow.framework;

public interface MemoryStatsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tensorflow.MemoryStats)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 host_temp_memory_size = 1;</code>
   */
  long getHostTempMemorySize();

  /**
   * <code>int64 device_temp_memory_size = 2;</code>
   */
  long getDeviceTempMemorySize();

  /**
   * <code>int64 host_persistent_memory_size = 3;</code>
   */
  long getHostPersistentMemorySize();

  /**
   * <code>int64 device_persistent_memory_size = 4;</code>
   */
  long getDevicePersistentMemorySize();

  /**
   * <code>repeated int64 host_persistent_tensor_alloc_ids = 5;</code>
   */
  java.util.List<Long> getHostPersistentTensorAllocIdsList();
  /**
   * <code>repeated int64 host_persistent_tensor_alloc_ids = 5;</code>
   */
  int getHostPersistentTensorAllocIdsCount();
  /**
   * <code>repeated int64 host_persistent_tensor_alloc_ids = 5;</code>
   */
  long getHostPersistentTensorAllocIds(int index);

  /**
   * <code>repeated int64 device_persistent_tensor_alloc_ids = 6;</code>
   */
  java.util.List<Long> getDevicePersistentTensorAllocIdsList();
  /**
   * <code>repeated int64 device_persistent_tensor_alloc_ids = 6;</code>
   */
  int getDevicePersistentTensorAllocIdsCount();
  /**
   * <code>repeated int64 device_persistent_tensor_alloc_ids = 6;</code>
   */
  long getDevicePersistentTensorAllocIds(int index);
}
