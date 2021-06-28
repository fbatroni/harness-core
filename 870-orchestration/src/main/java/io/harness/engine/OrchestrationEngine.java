package io.harness.engine;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.data.structure.HarnessStringUtils.emptyIfNull;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.pms.contracts.execution.Status.ERRORED;
import static io.harness.pms.contracts.execution.Status.RUNNING;
import static io.harness.springdata.SpringDataMongoUtils.setUnset;

import io.harness.OrchestrationPublisherName;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delay.DelayEventHelper;
import io.harness.engine.events.OrchestrationEventEmitter;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.executions.plan.PlanExecutionService;
import io.harness.engine.facilitation.FacilitationHelper;
import io.harness.engine.facilitation.RunPreFacilitationChecker;
import io.harness.engine.facilitation.SkipPreFacilitationChecker;
import io.harness.engine.facilitation.facilitator.publisher.FacilitateEventPublisher;
import io.harness.engine.observers.OrchestrationEndObserver;
import io.harness.engine.pms.advise.AdviseHandlerFactory;
import io.harness.engine.pms.advise.AdviserResponseHandler;
import io.harness.engine.pms.advise.NodeAdviseHelper;
import io.harness.engine.pms.resume.EngineWaitResumeCallback;
import io.harness.engine.pms.resume.NodeResumeHelper;
import io.harness.engine.pms.start.NodeStartHelper;
import io.harness.eraro.ResponseMessage;
import io.harness.exception.exceptionmanager.ExceptionManager;
import io.harness.execution.NodeExecution;
import io.harness.execution.NodeExecution.NodeExecutionKeys;
import io.harness.execution.PlanExecution;
import io.harness.execution.PlanExecution.PlanExecutionKeys;
import io.harness.logging.AutoLogContext;
import io.harness.observer.Subject;
import io.harness.pms.contracts.advisers.AdviseType;
import io.harness.pms.contracts.advisers.AdviserResponse;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.events.OrchestrationEvent;
import io.harness.pms.contracts.execution.events.OrchestrationEventType;
import io.harness.pms.contracts.facilitators.FacilitatorResponseProto;
import io.harness.pms.contracts.plan.PlanNodeProto;
import io.harness.pms.contracts.steps.io.StepResponseProto;
import io.harness.pms.contracts.steps.io.StepResponseProto.Builder;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.execution.utils.EngineExceptionUtils;
import io.harness.pms.execution.utils.LevelUtils;
import io.harness.pms.execution.utils.StatusUtils;
import io.harness.pms.expression.PmsEngineExpressionService;
import io.harness.pms.sdk.core.execution.NodeExecutionUtils;
import io.harness.pms.sdk.core.steps.io.StepResponseNotifyData;
import io.harness.serializer.ProtoUtils;
import io.harness.waiter.WaitNotifyEngine;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

/**
 * Please do not use this class outside of orchestration module. All the interactions with engine must be done via
 * {@link OrchestrationService}. This is for the internal workings of the engine
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@OwnedBy(HarnessTeam.PIPELINE)
@Singleton
public class OrchestrationEngine {
  @Inject private Injector injector;
  @Inject private WaitNotifyEngine waitNotifyEngine;
  @Inject @Named("EngineExecutorService") private ExecutorService executorService;
  @Inject private AdviseHandlerFactory adviseHandlerFactory;
  @Inject private DelayEventHelper delayEventHelper;
  @Inject private NodeExecutionService nodeExecutionService;
  @Inject private PlanExecutionService planExecutionService;
  @Inject private PmsEngineExpressionService pmsEngineExpressionService;
  @Inject @Named(OrchestrationPublisherName.PUBLISHER_NAME) String publisherName;
  @Inject private OrchestrationEventEmitter eventEmitter;
  @Inject private EndNodeExecutionHelper endNodeExecutionHelper;
  @Inject private ExceptionManager exceptionManager;
  @Inject private FacilitationHelper facilitationHelper;
  @Inject private FacilitateEventPublisher facilitateEventPublisher;
  @Inject private NodeStartHelper startHelper;
  @Inject private NodeAdviseHelper adviseHelper;
  @Inject private NodeResumeHelper resumeHelper;

  @Getter private final Subject<OrchestrationEndObserver> orchestrationEndSubject = new Subject<>();

  public void startNodeExecution(String nodeExecutionId) {
    NodeExecution nodeExecution = nodeExecutionService.get(nodeExecutionId);
    facilitateAndStartStep(nodeExecution.getAmbiance(), nodeExecution);
  }

  public void triggerExecution(Ambiance ambiance, PlanNodeProto node) {
    String uuid = generateUuid();
    NodeExecution previousNodeExecution = null;
    if (AmbianceUtils.obtainCurrentRuntimeId(ambiance) != null) {
      previousNodeExecution = nodeExecutionService.update(AmbianceUtils.obtainCurrentRuntimeId(ambiance),
          ops -> ops.set(NodeExecutionKeys.nextId, uuid).set(NodeExecutionKeys.endTs, System.currentTimeMillis()));
    }
    Ambiance cloned = reBuildAmbiance(ambiance, node, uuid);
    NodeExecution nodeExecution =
        NodeExecution.builder()
            .uuid(uuid)
            .node(node)
            .ambiance(cloned)
            .status(Status.QUEUED)
            .notifyId(previousNodeExecution == null ? null : previousNodeExecution.getNotifyId())
            .parentId(previousNodeExecution == null ? null : previousNodeExecution.getParentId())
            .previousId(previousNodeExecution == null ? null : previousNodeExecution.getUuid())
            .unitProgresses(new ArrayList<>())
            .build();
    nodeExecutionService.save(nodeExecution);
    executorService.submit(ExecutionEngineDispatcher.builder().ambiance(cloned).orchestrationEngine(this).build());
  }

  private Ambiance reBuildAmbiance(Ambiance ambiance, PlanNodeProto node, String uuid) {
    Ambiance cloned =
        AmbianceUtils.obtainCurrentRuntimeId(ambiance) == null ? ambiance : AmbianceUtils.cloneForFinish(ambiance);
    cloned = cloned.toBuilder().addLevels(LevelUtils.buildLevelFromPlanNode(uuid, node)).build();
    return cloned;
  }

  // Start to Facilitators
  private void facilitateAndStartStep(Ambiance ambiance, NodeExecution nodeExecution) {
    try (AutoLogContext ignore = AmbianceUtils.autoLogContext(ambiance)) {
      ExecutionCheck check = performPreFacilitationChecks(nodeExecution);
      if (!check.isProceed()) {
        log.info("Not Proceeding with  Execution. Reason : {}", check.getReason());
        return;
      }
      log.info("Proceeding with  Execution. Reason : {}", check.getReason());
      PlanNodeProto node = nodeExecution.getNode();
      String stepParameters = node.getStepParameters();
      boolean skipUnresolvedExpressionsCheck = node.getSkipUnresolvedExpressionsCheck();
      log.info("Starting to Resolve step parameters");
      Object resolvedStepParameters = stepParameters == null
          ? null
          : pmsEngineExpressionService.resolve(
              ambiance, NodeExecutionUtils.extractObject(stepParameters), skipUnresolvedExpressionsCheck);
      log.info("Step Parameter Resolution complete");

      log.info("Starting to Resolve step inputs");
      Object resolvedStepInputs = node.getStepInputs() == null
          ? null
          : pmsEngineExpressionService.resolve(
              ambiance, NodeExecutionUtils.extractObject(node.getStepInputs()), skipUnresolvedExpressionsCheck);
      log.info("Step Inputs Resolution complete");

      NodeExecution updatedNodeExecution =
          Preconditions.checkNotNull(nodeExecutionService.update(nodeExecution.getUuid(), ops -> {
            setUnset(ops, NodeExecutionKeys.resolvedStepParameters, resolvedStepParameters);
            setUnset(ops, NodeExecutionKeys.resolvedStepInputs, resolvedStepInputs);
          }));

      if (facilitationHelper.customFacilitatorPresent(node)) {
        facilitateEventPublisher.publishEvent(updatedNodeExecution.getUuid());
      } else {
        facilitationHelper.facilitateExecution(updatedNodeExecution);
      }
    } catch (Exception exception) {
      log.error("Exception Occurred in facilitateAndStartStep", exception);
      handleError(ambiance, exception);
    }
  }

  private ExecutionCheck performPreFacilitationChecks(NodeExecution nodeExecution) {
    // Ignore facilitation checks if node is retried
    if (!nodeExecution.getRetryIds().isEmpty()) {
      return ExecutionCheck.builder().proceed(true).reason("Node is retried.").build();
    }
    RunPreFacilitationChecker rChecker = injector.getInstance(RunPreFacilitationChecker.class);
    SkipPreFacilitationChecker sChecker = injector.getInstance(SkipPreFacilitationChecker.class);
    rChecker.setNextChecker(sChecker);
    return rChecker.check(nodeExecution);
  }

  public void facilitateExecution(String nodeExecutionId, FacilitatorResponseProto facilitatorResponse) {
    NodeExecution nodeExecution = nodeExecutionService.update(
        nodeExecutionId, ops -> ops.set(NodeExecutionKeys.mode, facilitatorResponse.getExecutionMode()));
    Ambiance ambiance = nodeExecution.getAmbiance();
    if (facilitatorResponse.getInitialWait() != null && facilitatorResponse.getInitialWait().getSeconds() != 0) {
      // Update Status
      Preconditions.checkNotNull(
          nodeExecutionService.updateStatusWithOps(AmbianceUtils.obtainCurrentRuntimeId(ambiance), Status.TIMED_WAITING,
              ops
              -> ops.set(NodeExecutionKeys.initialWaitDuration, facilitatorResponse.getInitialWait()),
              EnumSet.noneOf(Status.class)));
      String resumeId =
          delayEventHelper.delay(facilitatorResponse.getInitialWait().getSeconds(), Collections.emptyMap());
      waitNotifyEngine.waitForAllOn(publisherName,
          EngineWaitResumeCallback.builder().ambiance(ambiance).facilitatorResponse(facilitatorResponse).build(),
          resumeId);
      return;
    }
    startHelper.startNode(ambiance, facilitatorResponse);
  }

  public void handleStepResponse(@NonNull String nodeExecutionId, @NonNull StepResponseProto stepResponse) {
    NodeExecution nodeExecution = nodeExecutionService.get(nodeExecutionId);
    try {
      handleStepResponseInternal(nodeExecution, stepResponse);
    } catch (Exception ex) {
      log.error("Exception Occurred in handleStepResponse", ex);
      handleError(nodeExecution.getAmbiance(), ex);
    }
  }

  public void concludeNodeExecution(NodeExecution nodeExecution, Status status, EnumSet<Status> overrideStatusSet) {
    NodeExecution updatedNodeExecution = nodeExecutionService.updateStatusWithOps(nodeExecution.getUuid(), status,
        ops -> ops.set(NodeExecutionKeys.endTs, System.currentTimeMillis()), overrideStatusSet);
    if (updatedNodeExecution == null) {
      log.warn(
          "Cannot conclude node execution. Status update failed From :{}, To:{}", nodeExecution.getStatus(), status);
      return;
    }
    PlanNodeProto node = nodeExecution.getNode();
    if (isEmpty(node.getAdviserObtainmentsList())) {
      endTransition(nodeExecution);
      return;
    }
    adviseHelper.queueAdvisingEvent(updatedNodeExecution, nodeExecution.getStatus());
  }

  public void concludeNodeExecution(NodeExecution nodeExecution, Status status) {
    concludeNodeExecution(nodeExecution, status, EnumSet.noneOf(Status.class));
  }

  @VisibleForTesting
  void handleStepResponseInternal(@NonNull NodeExecution nodeExecution, @NonNull StepResponseProto stepResponse) {
    PlanNodeProto node = nodeExecution.getNode();
    if (isEmpty(node.getAdviserObtainmentsList())) {
      endNodeExecutionHelper.endNodeExecutionWithNoAdvisers(nodeExecution, stepResponse);
      return;
    }
    NodeExecution updatedNodeExecution =
        endNodeExecutionHelper.handleStepResponsePreAdviser(nodeExecution, stepResponse);
    if (updatedNodeExecution == null) {
      return;
    }
    adviseHelper.queueAdvisingEvent(updatedNodeExecution, nodeExecution.getStatus());
  }

  public void endTransition(NodeExecution nodeExecution) {
    nodeExecutionService.update(
        nodeExecution.getUuid(), ops -> ops.set(NodeExecutionKeys.endTs, System.currentTimeMillis()));
    if (isNotEmpty(nodeExecution.getNotifyId())) {
      PlanNodeProto planNode = nodeExecution.getNode();
      StepResponseNotifyData responseData = StepResponseNotifyData.builder()
                                                .nodeUuid(planNode.getUuid())
                                                .stepOutcomeRefs(nodeExecution.getOutcomeRefs())
                                                .failureInfo(nodeExecution.getFailureInfo())
                                                .identifier(planNode.getIdentifier())
                                                .group(planNode.getGroup())
                                                .status(nodeExecution.getStatus())
                                                .adviserResponse(nodeExecution.getAdviserResponse())
                                                .build();
      waitNotifyEngine.doneWith(nodeExecution.getNotifyId(), responseData);
    } else {
      log.info("Ending Execution");
      concludePlanExecution(nodeExecution);
    }
  }

  private void concludePlanExecution(NodeExecution nodeExecution) {
    Ambiance ambiance = nodeExecution.getAmbiance();
    Status status = planExecutionService.calculateStatus(ambiance.getPlanExecutionId());
    PlanExecution planExecution = planExecutionService.updateStatus(
        ambiance.getPlanExecutionId(), status, ops -> ops.set(PlanExecutionKeys.endTs, System.currentTimeMillis()));
    Document resolvedStepParameters = nodeExecution.getResolvedStepParameters();
    String stepParameters = null;
    if (resolvedStepParameters != null) {
      stepParameters = resolvedStepParameters.toJson();
    }
    eventEmitter.emitEvent(OrchestrationEvent.newBuilder()
                               .setAmbiance(Ambiance.newBuilder()
                                                .setPlanExecutionId(planExecution.getUuid())
                                                .putAllSetupAbstractions(planExecution.getSetupAbstractions() == null
                                                        ? Collections.emptyMap()
                                                        : planExecution.getSetupAbstractions())
                                                .build())
                               .setEventType(OrchestrationEventType.ORCHESTRATION_END)
                               .setStatus(nodeExecution.getStatus())
                               .setStepParameters(ByteString.copyFromUtf8(emptyIfNull(stepParameters)))
                               .setCreatedAt(ProtoUtils.unixMillisToTimestamp(System.currentTimeMillis()))
                               .build());
    orchestrationEndSubject.fireInform(OrchestrationEndObserver::onEnd, ambiance);
  }

  public void resume(String nodeExecutionId, Map<String, ByteString> response, boolean asyncError) {
    NodeExecution nodeExecution = nodeExecutionService.get(nodeExecutionId);
    Ambiance ambiance = nodeExecution.getAmbiance();
    try (AutoLogContext ignore = AmbianceUtils.autoLogContext(ambiance)) {
      if (!StatusUtils.resumableStatuses().contains(nodeExecution.getStatus())) {
        log.warn("NodeExecution is no longer in RESUMABLE state Uuid: {} Status {} ", nodeExecution.getUuid(),
            nodeExecution.getStatus());
        return;
      }
      if (nodeExecution.getStatus() != RUNNING) {
        log.info("Marking the nodeExecution with id {} as RUNNING", nodeExecutionId);
        nodeExecution = Preconditions.checkNotNull(
            nodeExecutionService.updateStatusWithOps(nodeExecutionId, RUNNING, null, EnumSet.noneOf(Status.class)));
      } else {
        log.warn("NodeExecution with id {} is already in Running status", nodeExecutionId);
      }
      resumeHelper.resume(nodeExecution, response, asyncError);
    } catch (Exception exception) {
      log.error("Exception Occurred in handling resume with nodeExecutionId {}", nodeExecutionId, exception);
      handleError(ambiance, exception);
    }
  }

  public void handleAdvise(String nodeExecutionId, AdviserResponse adviserResponse) {
    NodeExecution nodeExecution = nodeExecutionService.get(nodeExecutionId);
    if (adviserResponse.getType() == AdviseType.UNKNOWN) {
      log.warn("Got null advise for node execution with id {}", nodeExecutionId);
      endNodeExecutionHelper.endNodeForNullAdvise(nodeExecution);
      return;
    }
    NodeExecution updatedNodeExecution = nodeExecutionService.update(
        nodeExecutionId, ops -> ops.set(NodeExecutionKeys.adviserResponse, adviserResponse));
    AdviserResponseHandler adviserResponseHandler = adviseHandlerFactory.obtainHandler(adviserResponse.getType());
    adviserResponseHandler.handleAdvise(updatedNodeExecution, adviserResponse);
  }

  public void handleError(Ambiance ambiance, Exception exception) {
    try {
      Builder builder = StepResponseProto.newBuilder().setStatus(Status.FAILED);
      List<ResponseMessage> responseMessages = exceptionManager.buildResponseFromException(exception);
      if (isNotEmpty(responseMessages)) {
        builder.setFailureInfo(EngineExceptionUtils.transformResponseMessagesToFailureInfo(responseMessages));
      }
      NodeExecution nodeExecution = nodeExecutionService.get(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
      handleStepResponseInternal(nodeExecution, builder.build());
    } catch (Exception ex) {
      // Smile if you see irony in this
      log.error("This is very BAD!!!. Exception Occurred while handling Exception. Erroring out Execution", ex);
      errorOutPlanExecution(ambiance);
    }
  }

  void errorOutPlanExecution(Ambiance ambiance) {
    try {
      boolean nodeErrored = nodeExecutionService.errorOutActiveNodes(ambiance.getPlanExecutionId());
      if (!nodeErrored) {
        log.warn("No Nodes Can be marked as ERRORED");
      }
      planExecutionService.updateStatus(
          ambiance.getPlanExecutionId(), ERRORED, ops -> ops.set(PlanExecutionKeys.endTs, System.currentTimeMillis()));
    } catch (Exception ex) {
      log.error("Give Up!!!. Execution Will be stuck. We cannot do anything more", ex);
    }
  }
}
