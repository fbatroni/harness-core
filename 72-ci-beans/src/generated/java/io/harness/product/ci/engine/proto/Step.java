// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: product/ci/engine/proto/execution.proto

package io.harness.product.ci.engine.proto;

/**
 * Protobuf type {@code io.harness.product.ci.engine.proto.Step}
 */
@javax.annotation.Generated(value = "protoc", comments = "annotations:Step.java.pb.meta")
public final class Step extends com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:io.harness.product.ci.engine.proto.Step)
    StepOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use Step.newBuilder() to construct.
  private Step(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Step() {}

  @java.
  lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new Step();
  }

  @java.
  lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }
  private Step(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            io.harness.product.ci.engine.proto.ParallelStep.Builder subBuilder = null;
            if (stepCase_ == 1) {
              subBuilder = ((io.harness.product.ci.engine.proto.ParallelStep) step_).toBuilder();
            }
            step_ = input.readMessage(io.harness.product.ci.engine.proto.ParallelStep.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((io.harness.product.ci.engine.proto.ParallelStep) step_);
              step_ = subBuilder.buildPartial();
            }
            stepCase_ = 1;
            break;
          }
          case 18: {
            io.harness.product.ci.engine.proto.UnitStep.Builder subBuilder = null;
            if (stepCase_ == 2) {
              subBuilder = ((io.harness.product.ci.engine.proto.UnitStep) step_).toBuilder();
            }
            step_ = input.readMessage(io.harness.product.ci.engine.proto.UnitStep.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((io.harness.product.ci.engine.proto.UnitStep) step_);
              step_ = subBuilder.buildPartial();
            }
            stepCase_ = 2;
            break;
          }
          default: {
            if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return io.harness.product.ci.engine.proto.ExecutionOuterClass
        .internal_static_io_harness_product_ci_engine_proto_Step_descriptor;
  }

  @java.
  lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
    return io.harness.product.ci.engine.proto.ExecutionOuterClass
        .internal_static_io_harness_product_ci_engine_proto_Step_fieldAccessorTable.ensureFieldAccessorsInitialized(
            io.harness.product.ci.engine.proto.Step.class, io.harness.product.ci.engine.proto.Step.Builder.class);
  }

  private int stepCase_ = 0;
  private java.lang.Object step_;
  public enum StepCase implements com
  .google.protobuf.Internal.EnumLite, com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    PARALLEL(1), UNIT(2), STEP_NOT_SET(0);
    private final int value;
    private StepCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static StepCase valueOf(int value) {
      return forNumber(value);
    }

    public static StepCase forNumber(int value) {
      switch (value) {
        case 1:
          return PARALLEL;
        case 2:
          return UNIT;
        case 0:
          return STEP_NOT_SET;
        default:
          return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public StepCase getStepCase() {
    return StepCase.forNumber(stepCase_);
  }

  public static final int PARALLEL_FIELD_NUMBER = 1;
  /**
   * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
   * @return Whether the parallel field is set.
   */
  public boolean hasParallel() {
    return stepCase_ == 1;
  }
  /**
   * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
   * @return The parallel.
   */
  public io.harness.product.ci.engine.proto.ParallelStep getParallel() {
    if (stepCase_ == 1) {
      return (io.harness.product.ci.engine.proto.ParallelStep) step_;
    }
    return io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
  }
  /**
   * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
   */
  public io.harness.product.ci.engine.proto.ParallelStepOrBuilder getParallelOrBuilder() {
    if (stepCase_ == 1) {
      return (io.harness.product.ci.engine.proto.ParallelStep) step_;
    }
    return io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
  }

  public static final int UNIT_FIELD_NUMBER = 2;
  /**
   * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
   * @return Whether the unit field is set.
   */
  public boolean hasUnit() {
    return stepCase_ == 2;
  }
  /**
   * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
   * @return The unit.
   */
  public io.harness.product.ci.engine.proto.UnitStep getUnit() {
    if (stepCase_ == 2) {
      return (io.harness.product.ci.engine.proto.UnitStep) step_;
    }
    return io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
  }
  /**
   * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
   */
  public io.harness.product.ci.engine.proto.UnitStepOrBuilder getUnitOrBuilder() {
    if (stepCase_ == 2) {
      return (io.harness.product.ci.engine.proto.UnitStep) step_;
    }
    return io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1)
      return true;
    if (isInitialized == 0)
      return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
    if (stepCase_ == 1) {
      output.writeMessage(1, (io.harness.product.ci.engine.proto.ParallelStep) step_);
    }
    if (stepCase_ == 2) {
      output.writeMessage(2, (io.harness.product.ci.engine.proto.UnitStep) step_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1)
      return size;

    size = 0;
    if (stepCase_ == 1) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(
          1, (io.harness.product.ci.engine.proto.ParallelStep) step_);
    }
    if (stepCase_ == 2) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(
          2, (io.harness.product.ci.engine.proto.UnitStep) step_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof io.harness.product.ci.engine.proto.Step)) {
      return super.equals(obj);
    }
    io.harness.product.ci.engine.proto.Step other = (io.harness.product.ci.engine.proto.Step) obj;

    if (!getStepCase().equals(other.getStepCase()))
      return false;
    switch (stepCase_) {
      case 1:
        if (!getParallel().equals(other.getParallel()))
          return false;
        break;
      case 2:
        if (!getUnit().equals(other.getUnit()))
          return false;
        break;
      case 0:
      default:
    }
    if (!unknownFields.equals(other.unknownFields))
      return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    switch (stepCase_) {
      case 1:
        hash = (37 * hash) + PARALLEL_FIELD_NUMBER;
        hash = (53 * hash) + getParallel().hashCode();
        break;
      case 2:
        hash = (37 * hash) + UNIT_FIELD_NUMBER;
        hash = (53 * hash) + getUnit().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.harness.product.ci.engine.proto.Step parseFrom(java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(
      com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.product.ci.engine.proto.Step parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }
  public static io.harness.product.ci.engine.proto.Step parseDelimitedFrom(java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.product.ci.engine.proto.Step parseFrom(com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() {
    return newBuilder();
  }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.harness.product.ci.engine.proto.Step prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code io.harness.product.ci.engine.proto.Step}
   */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:io.harness.product.ci.engine.proto.Step)
      io.harness.product.ci.engine.proto.StepOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return io.harness.product.ci.engine.proto.ExecutionOuterClass
          .internal_static_io_harness_product_ci_engine_proto_Step_descriptor;
    }

    @java.
    lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return io.harness.product.ci.engine.proto.ExecutionOuterClass
          .internal_static_io_harness_product_ci_engine_proto_Step_fieldAccessorTable.ensureFieldAccessorsInitialized(
              io.harness.product.ci.engine.proto.Step.class, io.harness.product.ci.engine.proto.Step.Builder.class);
    }

    // Construct using io.harness.product.ci.engine.proto.Step.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      stepCase_ = 0;
      step_ = null;
      return this;
    }

    @java.
    lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return io.harness.product.ci.engine.proto.ExecutionOuterClass
          .internal_static_io_harness_product_ci_engine_proto_Step_descriptor;
    }

    @java.
    lang.Override
    public io.harness.product.ci.engine.proto.Step getDefaultInstanceForType() {
      return io.harness.product.ci.engine.proto.Step.getDefaultInstance();
    }

    @java.
    lang.Override
    public io.harness.product.ci.engine.proto.Step build() {
      io.harness.product.ci.engine.proto.Step result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.
    lang.Override
    public io.harness.product.ci.engine.proto.Step buildPartial() {
      io.harness.product.ci.engine.proto.Step result = new io.harness.product.ci.engine.proto.Step(this);
      if (stepCase_ == 1) {
        if (parallelBuilder_ == null) {
          result.step_ = step_;
        } else {
          result.step_ = parallelBuilder_.build();
        }
      }
      if (stepCase_ == 2) {
        if (unitBuilder_ == null) {
          result.step_ = step_;
        } else {
          result.step_ = unitBuilder_.build();
        }
      }
      result.stepCase_ = stepCase_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.harness.product.ci.engine.proto.Step) {
        return mergeFrom((io.harness.product.ci.engine.proto.Step) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.harness.product.ci.engine.proto.Step other) {
      if (other == io.harness.product.ci.engine.proto.Step.getDefaultInstance())
        return this;
      switch (other.getStepCase()) {
        case PARALLEL: {
          mergeParallel(other.getParallel());
          break;
        }
        case UNIT: {
          mergeUnit(other.getUnit());
          break;
        }
        case STEP_NOT_SET: {
          break;
        }
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
      io.harness.product.ci.engine.proto.Step parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.harness.product.ci.engine.proto.Step) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int stepCase_ = 0;
    private java.lang.Object step_;
    public StepCase getStepCase() {
      return StepCase.forNumber(stepCase_);
    }

    public Builder clearStep() {
      stepCase_ = 0;
      step_ = null;
      onChanged();
      return this;
    }

    private com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.ParallelStep,
        io.harness.product.ci.engine.proto.ParallelStep.Builder,
        io.harness.product.ci.engine.proto.ParallelStepOrBuilder> parallelBuilder_;
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     * @return Whether the parallel field is set.
     */
    public boolean hasParallel() {
      return stepCase_ == 1;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     * @return The parallel.
     */
    public io.harness.product.ci.engine.proto.ParallelStep getParallel() {
      if (parallelBuilder_ == null) {
        if (stepCase_ == 1) {
          return (io.harness.product.ci.engine.proto.ParallelStep) step_;
        }
        return io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
      } else {
        if (stepCase_ == 1) {
          return parallelBuilder_.getMessage();
        }
        return io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
      }
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public Builder setParallel(io.harness.product.ci.engine.proto.ParallelStep value) {
      if (parallelBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        step_ = value;
        onChanged();
      } else {
        parallelBuilder_.setMessage(value);
      }
      stepCase_ = 1;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public Builder setParallel(io.harness.product.ci.engine.proto.ParallelStep.Builder builderForValue) {
      if (parallelBuilder_ == null) {
        step_ = builderForValue.build();
        onChanged();
      } else {
        parallelBuilder_.setMessage(builderForValue.build());
      }
      stepCase_ = 1;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public Builder mergeParallel(io.harness.product.ci.engine.proto.ParallelStep value) {
      if (parallelBuilder_ == null) {
        if (stepCase_ == 1 && step_ != io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance()) {
          step_ = io.harness.product.ci.engine.proto.ParallelStep
                      .newBuilder((io.harness.product.ci.engine.proto.ParallelStep) step_)
                      .mergeFrom(value)
                      .buildPartial();
        } else {
          step_ = value;
        }
        onChanged();
      } else {
        if (stepCase_ == 1) {
          parallelBuilder_.mergeFrom(value);
        }
        parallelBuilder_.setMessage(value);
      }
      stepCase_ = 1;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public Builder clearParallel() {
      if (parallelBuilder_ == null) {
        if (stepCase_ == 1) {
          stepCase_ = 0;
          step_ = null;
          onChanged();
        }
      } else {
        if (stepCase_ == 1) {
          stepCase_ = 0;
          step_ = null;
        }
        parallelBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public io.harness.product.ci.engine.proto.ParallelStep.Builder getParallelBuilder() {
      return getParallelFieldBuilder().getBuilder();
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    public io.harness.product.ci.engine.proto.ParallelStepOrBuilder getParallelOrBuilder() {
      if ((stepCase_ == 1) && (parallelBuilder_ != null)) {
        return parallelBuilder_.getMessageOrBuilder();
      } else {
        if (stepCase_ == 1) {
          return (io.harness.product.ci.engine.proto.ParallelStep) step_;
        }
        return io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
      }
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.ParallelStep parallel = 1[json_name = "parallel"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.ParallelStep,
        io.harness.product.ci.engine.proto.ParallelStep.Builder,
        io.harness.product.ci.engine.proto.ParallelStepOrBuilder>
    getParallelFieldBuilder() {
      if (parallelBuilder_ == null) {
        if (!(stepCase_ == 1)) {
          step_ = io.harness.product.ci.engine.proto.ParallelStep.getDefaultInstance();
        }
        parallelBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.ParallelStep,
            io.harness.product.ci.engine.proto.ParallelStep.Builder,
            io.harness.product.ci.engine.proto.ParallelStepOrBuilder>(
            (io.harness.product.ci.engine.proto.ParallelStep) step_, getParentForChildren(), isClean());
        step_ = null;
      }
      stepCase_ = 1;
      onChanged();
      ;
      return parallelBuilder_;
    }

    private com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.UnitStep,
        io.harness.product.ci.engine.proto.UnitStep.Builder, io.harness.product.ci.engine.proto.UnitStepOrBuilder>
        unitBuilder_;
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     * @return Whether the unit field is set.
     */
    public boolean hasUnit() {
      return stepCase_ == 2;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     * @return The unit.
     */
    public io.harness.product.ci.engine.proto.UnitStep getUnit() {
      if (unitBuilder_ == null) {
        if (stepCase_ == 2) {
          return (io.harness.product.ci.engine.proto.UnitStep) step_;
        }
        return io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
      } else {
        if (stepCase_ == 2) {
          return unitBuilder_.getMessage();
        }
        return io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
      }
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public Builder setUnit(io.harness.product.ci.engine.proto.UnitStep value) {
      if (unitBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        step_ = value;
        onChanged();
      } else {
        unitBuilder_.setMessage(value);
      }
      stepCase_ = 2;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public Builder setUnit(io.harness.product.ci.engine.proto.UnitStep.Builder builderForValue) {
      if (unitBuilder_ == null) {
        step_ = builderForValue.build();
        onChanged();
      } else {
        unitBuilder_.setMessage(builderForValue.build());
      }
      stepCase_ = 2;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public Builder mergeUnit(io.harness.product.ci.engine.proto.UnitStep value) {
      if (unitBuilder_ == null) {
        if (stepCase_ == 2 && step_ != io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance()) {
          step_ = io.harness.product.ci.engine.proto.UnitStep
                      .newBuilder((io.harness.product.ci.engine.proto.UnitStep) step_)
                      .mergeFrom(value)
                      .buildPartial();
        } else {
          step_ = value;
        }
        onChanged();
      } else {
        if (stepCase_ == 2) {
          unitBuilder_.mergeFrom(value);
        }
        unitBuilder_.setMessage(value);
      }
      stepCase_ = 2;
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public Builder clearUnit() {
      if (unitBuilder_ == null) {
        if (stepCase_ == 2) {
          stepCase_ = 0;
          step_ = null;
          onChanged();
        }
      } else {
        if (stepCase_ == 2) {
          stepCase_ = 0;
          step_ = null;
        }
        unitBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public io.harness.product.ci.engine.proto.UnitStep.Builder getUnitBuilder() {
      return getUnitFieldBuilder().getBuilder();
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    public io.harness.product.ci.engine.proto.UnitStepOrBuilder getUnitOrBuilder() {
      if ((stepCase_ == 2) && (unitBuilder_ != null)) {
        return unitBuilder_.getMessageOrBuilder();
      } else {
        if (stepCase_ == 2) {
          return (io.harness.product.ci.engine.proto.UnitStep) step_;
        }
        return io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
      }
    }
    /**
     * <code>.io.harness.product.ci.engine.proto.UnitStep unit = 2[json_name = "unit"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.UnitStep,
        io.harness.product.ci.engine.proto.UnitStep.Builder, io.harness.product.ci.engine.proto.UnitStepOrBuilder>
    getUnitFieldBuilder() {
      if (unitBuilder_ == null) {
        if (!(stepCase_ == 2)) {
          step_ = io.harness.product.ci.engine.proto.UnitStep.getDefaultInstance();
        }
        unitBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<io.harness.product.ci.engine.proto.UnitStep,
            io.harness.product.ci.engine.proto.UnitStep.Builder, io.harness.product.ci.engine.proto.UnitStepOrBuilder>(
            (io.harness.product.ci.engine.proto.UnitStep) step_, getParentForChildren(), isClean());
        step_ = null;
      }
      stepCase_ = 2;
      onChanged();
      ;
      return unitBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:io.harness.product.ci.engine.proto.Step)
  }

  // @@protoc_insertion_point(class_scope:io.harness.product.ci.engine.proto.Step)
  private static final io.harness.product.ci.engine.proto.Step DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.harness.product.ci.engine.proto.Step();
  }

  public static io.harness.product.ci.engine.proto.Step getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Step> PARSER = new com.google.protobuf.AbstractParser<Step>() {
    @java.lang.Override
    public Step parsePartialFrom(
        com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Step(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Step> parser() {
    return PARSER;
  }

  @java.
  lang.Override
  public com.google.protobuf.Parser<Step> getParserForType() {
    return PARSER;
  }

  @java.
  lang.Override
  public io.harness.product.ci.engine.proto.Step getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
