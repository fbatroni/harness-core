package io.harness.data;

import io.harness.ambiance.LevelExecution;
import io.harness.persistence.CreatedAtAccess;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UuidAccess;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
@FieldNameConstants(innerTypeName = "OutcomeInstanceKeys")
@Indexes({
  @Index(options = @IndexOptions(name = "levelSetupIdx"),
      fields = { @Field("planExecutionId")
                 , @Field("levelExecutions.setupId"), @Field("name") })
  ,
      @Index(options = @IndexOptions(name = "levelRuntimeIdx"),
          fields = { @Field("planExecutionId")
                     , @Field("levelExecutions.runtimeId"), @Field("name") }),
      @Index(options = @IndexOptions(name = "planExecutionIdx"), fields = { @Field("ambiance.planExecutionId") }),
      @Index(
          options = @IndexOptions(name = "levelRuntimeUniqueIdx", unique = true), fields = { @Field("levelIndexKey") })
})
@Entity(value = "outcomeInstances", noClassnameStored = true)
public class OutcomeInstance implements PersistentEntity, UuidAccess, CreatedAtAccess {
  @Id String uuid;
  @NonNull String planExecutionId;
  @Singular List<LevelExecution> levelExecutions;
  @NonNull String name;
  Outcome outcome;
  long createdAt;
  @NonFinal @Getter(AccessLevel.NONE) String levelIndexKey;

  @UtilityClass
  public static final class OutcomeInstanceKeys {
    public static final String levelExecutionSetupId = "levelExecutions.setupId";
    public static final String levelExecutionRuntimeId = "levelExecutions.runtimeId";
  }

  @PrePersist
  void populateLevelIndexKey() {
    levelIndexKey =
        levelExecutions.stream().map(LevelExecution::getRuntimeId).collect(Collectors.joining("|")).concat("|" + name);
  }
}
