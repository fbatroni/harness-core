package io.harness.cvng.analysis.entities;

import io.harness.annotation.HarnessEntity;
import io.harness.annotation.StoreIn;
import io.harness.cvng.analysis.beans.Risk;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.DbAliases;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UuidAware;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(innerTypeName = "LogAnalysisResultKeys")
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(value = "logAnalysisResults", noClassnameStored = true)
@HarnessEntity(exportable = false)
@StoreIn(DbAliases.CVNG)
public final class LogAnalysisResult implements PersistentEntity, UuidAware, CreatedAtAware, UpdatedAtAware {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("query_idx")
                 .field(LogAnalysisResultKeys.verificationTaskId)
                 .field(LogAnalysisResultKeys.analysisEndTime)
                 .build())
        .build();
  }

  @Id private String uuid;
  @FdIndex private long createdAt;
  @FdIndex private long lastUpdatedAt;
  private String verificationTaskId;
  private Instant analysisStartTime;
  private Instant analysisEndTime;
  @FdIndex private String accountId;

  private double overallRisk;
  private List<AnalysisResult> logAnalysisResults;

  @Data
  @Builder
  @FieldNameConstants(innerTypeName = "AnalysisResultKeys")
  public static class AnalysisResult {
    private long label;
    private LogAnalysisTag tag;
    private int count;
    private double riskScore;
    public Risk getRisk() {
      return Risk.getRiskFromRiskScore(riskScore);
    }

    public void setTag(LogAnalysisTag tag) {
      this.tag = tag;
      if (tag.equals(LogAnalysisTag.KNOWN)) {
        this.riskScore = 0.0;
      } else if (tag.equals(LogAnalysisTag.UNKNOWN)) {
        this.riskScore = 1.0;
      }
    }
  }

  public enum LogAnalysisTag {
    KNOWN(0),
    UNEXPECTED(1),
    UNKNOWN(2);

    private Integer severity;

    LogAnalysisTag(int severity) {
      this.severity = severity;
    }

    public boolean isMoreSevereThan(LogAnalysisTag other) {
      return this.severity > other.severity;
    }

    public static Set<LogAnalysisTag> getAnomalousTags() {
      return Sets.newHashSet(UNKNOWN, UNEXPECTED);
    }
  }
}
