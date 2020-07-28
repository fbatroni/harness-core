// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: product/ci/engine/proto/execution.proto

package io.harness.product.ci.engine.proto;

@javax.annotation.Generated(value = "protoc", comments = "annotations:UnitStepOrBuilder.java.pb.meta")
public interface UnitStepOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.harness.product.ci.engine.proto.UnitStep)
    com.google.protobuf.MessageOrBuilder {
  /**
   * <code>string id = 1[json_name = "id"];</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1[json_name = "id"];</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString getIdBytes();

  /**
   * <code>string display_name = 2[json_name = "displayName"];</code>
   * @return The displayName.
   */
  java.lang.String getDisplayName();
  /**
   * <code>string display_name = 2[json_name = "displayName"];</code>
   * @return The bytes for displayName.
   */
  com.google.protobuf.ByteString getDisplayNameBytes();

  /**
   * <code>.io.harness.product.ci.engine.proto.RunStep run = 3[json_name = "run"];</code>
   * @return Whether the run field is set.
   */
  boolean hasRun();
  /**
   * <code>.io.harness.product.ci.engine.proto.RunStep run = 3[json_name = "run"];</code>
   * @return The run.
   */
  io.harness.product.ci.engine.proto.RunStep getRun();
  /**
   * <code>.io.harness.product.ci.engine.proto.RunStep run = 3[json_name = "run"];</code>
   */
  io.harness.product.ci.engine.proto.RunStepOrBuilder getRunOrBuilder();

  /**
   * <code>.io.harness.product.ci.engine.proto.SaveCacheStep save_cache = 4[json_name = "saveCache"];</code>
   * @return Whether the saveCache field is set.
   */
  boolean hasSaveCache();
  /**
   * <code>.io.harness.product.ci.engine.proto.SaveCacheStep save_cache = 4[json_name = "saveCache"];</code>
   * @return The saveCache.
   */
  io.harness.product.ci.engine.proto.SaveCacheStep getSaveCache();
  /**
   * <code>.io.harness.product.ci.engine.proto.SaveCacheStep save_cache = 4[json_name = "saveCache"];</code>
   */
  io.harness.product.ci.engine.proto.SaveCacheStepOrBuilder getSaveCacheOrBuilder();

  /**
   * <code>.io.harness.product.ci.engine.proto.RestoreCacheStep restore_cache = 5[json_name = "restoreCache"];</code>
   * @return Whether the restoreCache field is set.
   */
  boolean hasRestoreCache();
  /**
   * <code>.io.harness.product.ci.engine.proto.RestoreCacheStep restore_cache = 5[json_name = "restoreCache"];</code>
   * @return The restoreCache.
   */
  io.harness.product.ci.engine.proto.RestoreCacheStep getRestoreCache();
  /**
   * <code>.io.harness.product.ci.engine.proto.RestoreCacheStep restore_cache = 5[json_name = "restoreCache"];</code>
   */
  io.harness.product.ci.engine.proto.RestoreCacheStepOrBuilder getRestoreCacheOrBuilder();

  /**
   * <code>.io.harness.product.ci.engine.proto.PublishArtifactsStep publish_artifacts = 6[json_name =
   * "publishArtifacts"];</code>
   * @return Whether the publishArtifacts field is set.
   */
  boolean hasPublishArtifacts();
  /**
   * <code>.io.harness.product.ci.engine.proto.PublishArtifactsStep publish_artifacts = 6[json_name =
   * "publishArtifacts"];</code>
   * @return The publishArtifacts.
   */
  io.harness.product.ci.engine.proto.PublishArtifactsStep getPublishArtifacts();
  /**
   * <code>.io.harness.product.ci.engine.proto.PublishArtifactsStep publish_artifacts = 6[json_name =
   * "publishArtifacts"];</code>
   */
  io.harness.product.ci.engine.proto.PublishArtifactsStepOrBuilder getPublishArtifactsOrBuilder();

  public io.harness.product.ci.engine.proto.UnitStep.StepCase getStepCase();
}
