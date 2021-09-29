package io.harness.engine.pms.execution.strategy.plan;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.PRASHANT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.OrchestrationTestBase;
import io.harness.category.element.UnitTests;
import io.harness.engine.OrchestrationEngine;
import io.harness.engine.executions.plan.PlanExecutionMetadataService;
import io.harness.execution.PlanExecutionMetadata;
import io.harness.plan.Plan;
import io.harness.plan.PlanNode;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.plan.TriggeredBy;
import io.harness.pms.contracts.steps.StepType;
import io.harness.rule.Owner;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PlanExecutionStrategyTest extends OrchestrationTestBase {
  private static final String DUMMY_NODE_1_ID = generateUuid();
  private static final String DUMMY_NODE_2_ID = generateUuid();
  private static final String DUMMY_NODE_3_ID = generateUuid();

  private static final StepType DUMMY_STEP_TYPE = StepType.newBuilder().setType("DUMMY").build();

  private static final TriggeredBy triggeredBy =
      TriggeredBy.newBuilder().putExtraInfo("email", PRASHANT).setIdentifier(PRASHANT).setUuid(generateUuid()).build();

  @Mock @Named("EngineExecutorService") ExecutorService executorService;
  @Mock OrchestrationEngine orchestrationEngine;
  @Mock PlanExecutionMetadataService planExecutionMetadataService;
  @Inject @InjectMocks PlanExecutionStrategy executionStrategy;

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestTriggerNode() {
    String planExecutionId = generateUuid();
    Ambiance ambiance = Ambiance.newBuilder()
                            .setPlanExecutionId(planExecutionId)
                            .putAllSetupAbstractions(prepareInputArgs())
                            .addLevels(Level.newBuilder().setRuntimeId(generateUuid()).build())
                            .build();
    PlanNode startingNode = PlanNode.builder()
                                .uuid(DUMMY_NODE_1_ID)
                                .name("Dummy Node 1")
                                .stepType(DUMMY_STEP_TYPE)
                                .identifier("dummy1")
                                .build();
    Plan plan = Plan.builder()
                    .planNode(startingNode)
                    .planNode(PlanNode.builder()
                                  .uuid(DUMMY_NODE_2_ID)
                                  .name("Dummy Node 2")
                                  .stepType(DUMMY_STEP_TYPE)
                                  .identifier("dummy2")
                                  .build())
                    .planNode(PlanNode.builder()
                                  .uuid(DUMMY_NODE_3_ID)
                                  .name("Dummy Node 3")
                                  .stepType(DUMMY_STEP_TYPE)
                                  .identifier("dummy3")
                                  .build())
                    .startingNodeId(DUMMY_NODE_1_ID)
                    .build();

    when(planExecutionMetadataService.findByPlanExecutionId(planExecutionId))
        .thenReturn(Optional.of(PlanExecutionMetadata.builder().planExecutionId(planExecutionId).build()));

    executionStrategy.triggerNode(ambiance, plan);
    // TODO: make this better
    ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(executorService).submit(runnableArgumentCaptor.capture());
    assertThat(runnableArgumentCaptor.getValue()).isNotNull();
  }

  private static Map<String, String> prepareInputArgs() {
    return ImmutableMap.of("accountId", "kmpySmUISimoRrJL6NL73w", "appId", "XEsfW6D_RJm1IaGpDidD3g", "userId",
        triggeredBy.getUuid(), "userName", triggeredBy.getIdentifier(), "userEmail",
        triggeredBy.getExtraInfoOrThrow("email"));
  }
}