package io.harness.ngtriggers.beans.entity;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotation.HarnessEntity;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.validator.EntityIdentifier;
import io.harness.data.validator.EntityName;
import io.harness.iterator.PersistentNGCronIterable;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.core.common.beans.NGTag;
import io.harness.ngtriggers.beans.entity.metadata.NGTriggerMetadata;
import io.harness.ngtriggers.beans.source.NGTriggerType;
import io.harness.ngtriggers.beans.target.TargetType;
import io.harness.persistence.PersistentEntity;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@FieldNameConstants(innerTypeName = "NGTriggerEntityKeys")
@Entity(value = "triggersNG", noClassnameStored = true)
@Document("triggersNG")
@TypeAlias("triggersNG")
@HarnessEntity(exportable = true)
@Slf4j
@OwnedBy(PIPELINE)
public class NGTriggerEntity implements PersistentEntity, PersistentNGCronIterable {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(
            CompoundMongoIndex.builder()
                .name(
                    "unique_accountId_organizationIdentifier_projectIdentifier_targetIdentifier_triggerType_identifier")
                .unique(true)
                .field(NGTriggerEntityKeys.accountId)
                .field(NGTriggerEntityKeys.orgIdentifier)
                .field(NGTriggerEntityKeys.projectIdentifier)
                .field(NGTriggerEntityKeys.targetIdentifier)
                .field(NGTriggerEntityKeys.targetType)
                .field(NGTriggerEntityKeys.identifier)
                .build(),
            CompoundMongoIndex.builder()
                .name("unique_accountId_organizationIdentifier_projectIdentifier_identifier")
                .unique(false)
                .field(NGTriggerEntityKeys.accountId)
                .field(NGTriggerEntityKeys.orgIdentifier)
                .field(NGTriggerEntityKeys.projectIdentifier)
                .field(NGTriggerEntityKeys.identifier)
                .build(),
            CompoundMongoIndex.builder()
                .name("type_repoUrl")
                .field(NGTriggerEntityKeys.type)
                .field("metadata.webhook.git.connectorIdentifier")
                .field(NGTriggerEntityKeys.accountId)
                .field(NGTriggerEntityKeys.orgIdentifier)
                .field(NGTriggerEntityKeys.projectIdentifier)
                .build(),
            CompoundMongoIndex.builder()
                .name("accId_sourcerepo_index")
                .field(NGTriggerEntityKeys.accountId)
                .field("metadata.webhook.type")
                .build(),
            CompoundMongoIndex.builder()
                .name("accId_signature_index")
                .field(NGTriggerEntityKeys.accountId)
                .field(NGTriggerEntityKeys.signature)
                .build())
        .build();
  }

  @Id @org.mongodb.morphia.annotations.Id String uuid;
  @EntityName String name;
  @EntityIdentifier @NotEmpty String identifier;
  @Size(max = 1024) String description;
  @NotEmpty String yaml;
  @NotEmpty NGTriggerType type;
  String status;
  @NotEmpty String accountId;
  @NotEmpty String orgIdentifier;
  @NotEmpty String projectIdentifier;
  @NotEmpty String targetIdentifier;
  @NotEmpty TargetType targetType;
  String signature;

  @NotEmpty NGTriggerMetadata metadata;
  ValidationStatus validationStatus;
  @CreatedDate Long createdAt;
  @LastModifiedDate Long lastModifiedAt;
  @Version Long version;
  @Builder.Default Boolean deleted = Boolean.FALSE;
  @Singular @Size(max = 128) List<NGTag> tags;
  @Builder.Default Boolean enabled = Boolean.TRUE;
  @FdIndex private List<Long> nextIterations; // List of activation times for cron triggers
  @Builder.Default Long ymlVersion = Long.valueOf(2);

  @Override
  public List<Long> recalculateNextIterations(String fieldName, boolean skipMissed, long throttled) {
    if (metadata.getCron() == null || nextIterations == null) {
      return new ArrayList<>();
    }
    try {
      String cronExpr = metadata.getCron().getExpression();
      expandNextIterations(skipMissed, throttled, cronExpr, nextIterations);
    } catch (Exception e) {
      log.error("Failed to schedule executions for trigger {}", uuid, e);
      throw e;
    }
    return nextIterations;
  }

  @Override
  public Long obtainNextIteration(String fieldName) {
    if (metadata.getCron() == null || nextIterations == null) {
      return null;
    }
    return nextIterations.get(0);
  }
}
