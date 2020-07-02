package io.harness.cvng.core.entities;

import io.harness.cvng.beans.TimeRange;
import io.harness.cvng.models.VerificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class LogCVConfig extends CVConfig {
  private TimeRange baseline;
  private String query;

  @Override
  public VerificationType getVerificationType() {
    return VerificationType.LOG;
  }
}
