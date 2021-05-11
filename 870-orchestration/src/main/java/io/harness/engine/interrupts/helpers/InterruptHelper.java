package io.harness.engine.interrupts.helpers;

import io.harness.data.structure.EmptyPredicate;
import io.harness.engine.pms.tasks.TaskExecutor;
import io.harness.exception.InvalidRequestException;
import io.harness.execution.NodeExecution;
import io.harness.logging.UnitProgress;
import io.harness.logging.UnitStatus;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.ExecutableResponse;
import io.harness.pms.contracts.execution.TaskChainExecutableResponse;
import io.harness.pms.contracts.execution.TaskExecutableResponse;
import io.harness.pms.contracts.execution.tasks.TaskCategory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class InterruptHelper {
  @Inject private Map<TaskCategory, TaskExecutor> taskExecutorMap;

  @VisibleForTesting
  public static List<UnitProgress> evaluateUnitProgresses(NodeExecution nodeExecution, UnitStatus unitStatus) {
    List<UnitProgress> unitProgressList = new ArrayList<>();
    if (!EmptyPredicate.isEmpty(nodeExecution.getUnitProgresses())) {
      for (UnitProgress up : nodeExecution.getUnitProgresses()) {
        if (isFinalUnitProgress(up.getStatus())) {
          unitProgressList.add(up);
        } else {
          unitProgressList.add(up.toBuilder().setStatus(unitStatus).setEndTime(System.currentTimeMillis()).build());
        }
      }
    }
    return unitProgressList;
  }

  private static boolean isFinalUnitProgress(UnitStatus status) {
    return EnumSet.of(UnitStatus.FAILURE, UnitStatus.SKIPPED, UnitStatus.SUCCESS).contains(status);
  }

  public boolean discontinueTaskIfRequired(NodeExecution nodeExecution) {
    Ambiance ambiance = nodeExecution.getAmbiance();
    ExecutableResponse executableResponse = nodeExecution.obtainLatestExecutableResponse();
    if (executableResponse != null && nodeExecution.isTaskSpawningMode()) {
      String taskId;
      TaskCategory taskCategory;
      switch (executableResponse.getResponseCase()) {
        case TASK:
          TaskExecutableResponse taskExecutableResponse = executableResponse.getTask();
          taskId = taskExecutableResponse.getTaskId();
          taskCategory = taskExecutableResponse.getTaskCategory();
          break;
        case TASKCHAIN:
          TaskChainExecutableResponse taskChainExecutableResponse = executableResponse.getTaskChain();
          taskId = taskChainExecutableResponse.getTaskId();
          taskCategory = taskChainExecutableResponse.getTaskCategory();
          break;
        default:
          throw new InvalidRequestException("Executable Response should contain either task or taskChain");
      }
      TaskExecutor executor = taskExecutorMap.get(taskCategory);
      return executor.abortTask(ambiance.getSetupAbstractionsMap(), taskId);
    }
    return true;
  }
}
