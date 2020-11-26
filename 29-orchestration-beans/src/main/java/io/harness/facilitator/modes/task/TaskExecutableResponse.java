package io.harness.facilitator.modes.task;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.facilitator.modes.TaskSpawningExecutableResponse;
import io.harness.tasks.TaskMode;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(CDC)
@Redesign
@Value
@Builder
@TypeAlias("taskExecutableResponse")
public class TaskExecutableResponse implements TaskSpawningExecutableResponse {
  String taskId;
  TaskMode taskMode;
}
