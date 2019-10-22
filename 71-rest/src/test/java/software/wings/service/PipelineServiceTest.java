package software.wings.service;

import static io.harness.beans.PageRequest.PageRequestBuilder.aPageRequest;
import static io.harness.beans.PageResponse.PageResponseBuilder.aPageResponse;
import static io.harness.beans.SearchFilter.Operator.EQ;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.wings.beans.Application.Builder.anApplication;
import static software.wings.beans.BasicOrchestrationWorkflow.BasicOrchestrationWorkflowBuilder.aBasicOrchestrationWorkflow;
import static software.wings.beans.BuildWorkflow.BuildOrchestrationWorkflowBuilder.aBuildOrchestrationWorkflow;
import static software.wings.beans.CanaryOrchestrationWorkflow.CanaryOrchestrationWorkflowBuilder.aCanaryOrchestrationWorkflow;
import static software.wings.beans.EntityType.ARTIFACT;
import static software.wings.beans.EntityType.ENVIRONMENT;
import static software.wings.beans.EntityType.INFRASTRUCTURE_MAPPING;
import static software.wings.beans.EntityType.SERVICE;
import static software.wings.beans.EntityType.SSH_PASSWORD;
import static software.wings.beans.EntityType.SSH_USER;
import static software.wings.beans.PhaseStep.PhaseStepBuilder.aPhaseStep;
import static software.wings.beans.PhaseStepType.POST_DEPLOYMENT;
import static software.wings.beans.PhaseStepType.PRE_DEPLOYMENT;
import static software.wings.beans.PipelineExecution.Builder.aPipelineExecution;
import static software.wings.beans.Variable.VariableBuilder.aVariable;
import static software.wings.beans.Workflow.WorkflowBuilder.aWorkflow;
import static software.wings.beans.WorkflowPhase.WorkflowPhaseBuilder.aWorkflowPhase;
import static software.wings.sm.StateType.APPROVAL;
import static software.wings.sm.StateType.ENV_STATE;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.APP_NAME;
import static software.wings.utils.WingsTestConstants.ENV_ID;
import static software.wings.utils.WingsTestConstants.ENV_NAME;
import static software.wings.utils.WingsTestConstants.INFRA_MAPPING_ID;
import static software.wings.utils.WingsTestConstants.INFRA_NAME;
import static software.wings.utils.WingsTestConstants.PIPELINE_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_NAME;
import static software.wings.utils.WingsTestConstants.WORKFLOW_ID;
import static software.wings.utils.WingsTestConstants.WORKFLOW_NAME;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import io.harness.beans.ExecutionStatus;
import io.harness.beans.OrchestrationWorkflowType;
import io.harness.beans.PageRequest;
import io.harness.beans.PageResponse;
import io.harness.beans.WorkflowType;
import io.harness.category.element.UnitTests;
import io.harness.eraro.ErrorCode;
import io.harness.exception.InvalidArgumentsException;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import io.harness.limits.Action;
import io.harness.limits.ActionType;
import io.harness.limits.LimitCheckerFactory;
import io.harness.persistence.HQuery;
import io.harness.resource.Loader;
import io.harness.serializer.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FieldEnd;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import software.wings.WingsBaseTest;
import software.wings.api.DeploymentType;
import software.wings.beans.ArtifactVariable;
import software.wings.beans.EntityType;
import software.wings.beans.EnvSummary;
import software.wings.beans.Environment.EnvironmentType;
import software.wings.beans.FailureStrategy;
import software.wings.beans.Pipeline;
import software.wings.beans.PipelineExecution;
import software.wings.beans.PipelineStage;
import software.wings.beans.PipelineStage.PipelineStageElement;
import software.wings.beans.RepairActionCode;
import software.wings.beans.Service;
import software.wings.beans.Variable;
import software.wings.beans.Workflow;
import software.wings.beans.WorkflowExecution;
import software.wings.beans.deployment.DeploymentMetadata;
import software.wings.beans.deployment.DeploymentMetadata.Include;
import software.wings.dl.WingsPersistence;
import software.wings.scheduler.BackgroundJobScheduler;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.ResourceLookupService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.TriggerService;
import software.wings.service.intfc.WorkflowExecutionService;
import software.wings.service.intfc.WorkflowService;
import software.wings.service.intfc.yaml.YamlDirectoryService;
import software.wings.sm.StateMachine;
import software.wings.sm.StateType;
import software.wings.utils.WingsTestConstants.MockChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineServiceTest extends WingsBaseTest {
  private static final String PIPELINE = Loader.load("pipeline/dry_run.json");

  @Mock private AppService appService;
  @Mock private TriggerService triggerService;
  @Mock private WingsPersistence wingsPersistence;
  @Mock private WorkflowService workflowService;
  @Mock private WorkflowExecutionService workflowExecutionService;
  @Mock private UpdateOperations<Pipeline> updateOperations;
  @Mock private HQuery<PipelineExecution> query;
  @Mock private HQuery<Pipeline> pipelineQuery;
  @Mock private BackgroundJobScheduler jobScheduler;
  @Mock private YamlDirectoryService yamlDirectoryService;
  @Mock private MorphiaIterator<Pipeline, Pipeline> pipelineIterator;
  @Mock private ServiceResourceService serviceResourceService;
  @Mock private EnvironmentService environmentService;
  @Mock private LimitCheckerFactory limitCheckerFactory;

  @Mock Query<Pipeline> pquery;
  @Mock private FieldEnd end;

  @Inject @InjectMocks private PipelineService pipelineService;
  @Inject @InjectMocks ResourceLookupService resourceLookupService;

  @Captor private ArgumentCaptor<Pipeline> pipelineArgumentCaptor;
  @Captor private ArgumentCaptor<StateMachine> stateMachineArgumentCaptor;

  @Before
  public void setUp() {
    when(wingsPersistence.createQuery(PipelineExecution.class)).thenReturn(query);

    when(wingsPersistence.createQuery(Pipeline.class)).thenReturn(pipelineQuery);
    when(pipelineQuery.filter(any(), any())).thenReturn(pipelineQuery);
    when(pipelineQuery.project(anyString(), anyBoolean())).thenReturn(pipelineQuery);
    when(wingsPersistence.createUpdateOperations(Pipeline.class)).thenReturn(updateOperations);
    when(appService.get(APP_ID)).thenReturn(anApplication().uuid(APP_ID).name(APP_NAME).build());
    when(updateOperations.set(any(), any())).thenReturn(updateOperations);
    when(updateOperations.unset(any())).thenReturn(updateOperations);
    when(serviceResourceService.fetchServicesByUuids(APP_ID, Arrays.asList(SERVICE_ID)))
        .thenReturn(Arrays.asList(Service.builder().name(SERVICE_NAME).uuid(SERVICE_ID).build()));
    when(workflowService.fetchDeploymentMetadata(
             anyString(), any(Workflow.class), anyMap(), anyList(), anyList(), anyVararg()))
        .thenReturn(DeploymentMetadata.builder()
                        .artifactRequiredServiceIds(asList(SERVICE_ID))
                        .envIds(asList(ENV_ID))
                        .deploymentTypes(asList(DeploymentType.SSH))
                        .build());
    when(environmentService.obtainEnvironmentSummaries(APP_ID, asList(ENV_ID)))
        .thenReturn(
            asList(EnvSummary.builder().name(ENV_NAME).uuid(ENV_ID).environmentType(EnvironmentType.PROD).build()));
  }

  @Test
  @Category(UnitTests.class)
  public void shouldCreatePipelineFromJson() {
    when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
        .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));

    String appId = "BB1xpV5rSmGHersn1KwCnA";
    String workflowId = "7-fkKHxHS7SsDeWbRa2zCw";

    Pipeline pipeline = JsonUtils.asObject(PIPELINE, Pipeline.class);
    when(workflowService.readWorkflow(appId, workflowId))
        .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());

    when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));
    when(workflowService.fetchDeploymentMetadata(any(), any(Workflow.class), anyMap(), any(), any(), any()))
        .thenReturn(DeploymentMetadata.builder().build());
    pipelineService.save(pipeline);

    verify(wingsPersistence).save(pipelineArgumentCaptor.capture());
    assertThat(pipelineArgumentCaptor.getValue()).isNotNull();
  }

  @Test
  @Category(UnitTests.class)
  public void shouldCreateLargePipeline() {
    when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
        .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));

    List<String> workflowIds = new ArrayList<>();
    List<PipelineStage> pipelineStages = new ArrayList<>();
    for (int index = 0; index < 60; index++) {
      String uuid = generateUuid();
      workflowIds.add(uuid);
      when(workflowService.readWorkflow(APP_ID, uuid))
          .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());

      Map<String, Object> properties = new HashMap<>();
      properties.put("envId", generateUuid());
      properties.put("workflowId", uuid);
      PipelineStage pipelineStage = PipelineStage.builder()
                                        .pipelineStageElements(asList(PipelineStageElement.builder()
                                                                          .name("STAGE" + index)
                                                                          .type(ENV_STATE.name())
                                                                          .properties(properties)
                                                                          .build()))
                                        .build();
      if (index % 16 == 0) {
        pipelineStage.setParallel(false);
      } else {
        pipelineStage.setParallel(true);
      }
      pipelineStages.add(pipelineStage);
    }

    Pipeline pipeline =
        Pipeline.builder().name("pipeline1").appId(APP_ID).uuid(PIPELINE_ID).pipelineStages(pipelineStages).build();

    when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

    pipelineService.save(pipeline);

    verify(wingsPersistence).save(pipelineArgumentCaptor.capture());
    assertThat(pipelineArgumentCaptor.getValue()).isNotNull();
  }

  @Test
  @Category(UnitTests.class)
  public void shouldCreatePipeline() {
    when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
        .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));

    PipelineStage pipelineStage = prepareStageSimple();

    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))
                            .failureStrategies(asList(failureStrategy))
                            .build();

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());
    when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

    pipelineService.save(pipeline);

    verify(wingsPersistence).save(pipelineArgumentCaptor.capture());
    Pipeline argumentCaptorValue = pipelineArgumentCaptor.getValue();
    assertThat(argumentCaptorValue)
        .isNotNull()
        .extracting(Pipeline::getFailureStrategies)
        .asList()
        .containsExactly(failureStrategy);
    assertThat(argumentCaptorValue.getKeywords())
        .isNotNull()
        .contains(WorkflowType.PIPELINE.name().toLowerCase(), pipeline.getName().toLowerCase());
  }

  @Test
  @Category(UnitTests.class)
  public void shouldUpdatePipeline() {
    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = getPipelineSimple(failureStrategy, prepareStageSimple());

    Pipeline updatedPipeline = pipelineService.update(pipeline, false);

    assertThat(updatedPipeline)
        .isNotNull()
        .extracting(Pipeline::getFailureStrategies)
        .asList()
        .containsExactly(failureStrategy);

    verify(wingsPersistence, times(2)).getWithAppId(Pipeline.class, pipeline.getAppId(), pipeline.getUuid());
    verify(wingsPersistence).createQuery(Pipeline.class);
    verify(pipelineQuery, times(2)).filter(any(), any());
    verify(wingsPersistence).createUpdateOperations(Pipeline.class);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldUpdatePipelineWithDisableAssertion() {
    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();

    Pipeline pipeline = getPipelineSimple(failureStrategy, prepareStageDisableAssertion(ENV_ID, WORKFLOW_ID));

    Pipeline updatedPipeline = pipelineService.update(pipeline, false);

    assertThat(updatedPipeline)
        .isNotNull()
        .extracting(Pipeline::getFailureStrategies)
        .asList()
        .containsExactly(failureStrategy);

    verify(wingsPersistence, times(2)).getWithAppId(Pipeline.class, pipeline.getAppId(), pipeline.getUuid());
    verify(wingsPersistence).createQuery(Pipeline.class);
    verify(pipelineQuery, times(2)).filter(any(), any());
    verify(wingsPersistence).createUpdateOperations(Pipeline.class);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldNotUpdatePipelineWithDisableAssertion() {
    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = getPipelineSimple(
        failureStrategy, prepareStageApprovalDisableAssertion(), prepareStageDisableAssertion(ENV_ID, WORKFLOW_ID));

    assertThatExceptionOfType(InvalidArgumentsException.class)
        .isThrownBy(() -> pipelineService.update(pipeline, false));
  }

  @NotNull
  private Pipeline getPipelineSimple(FailureStrategy failureStrategy, PipelineStage... pipelineStages) {
    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStages))
                            .failureStrategies(asList(failureStrategy))
                            .build();

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());
    when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

    pipeline.setName("Changed Pipeline");
    pipeline.setDescription("Description changed");

    when(wingsPersistence.getWithAppId(Pipeline.class, pipeline.getAppId(), pipeline.getUuid())).thenReturn(pipeline);
    return pipeline;
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetPipeline() {
    mockPipeline();

    Pipeline pipeline = pipelineService.readPipeline(APP_ID, PIPELINE_ID, false);
    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetPipelineWithServices() {
    mockPipeline();

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean()))
        .thenReturn(aWorkflow()
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .orchestrationWorkflow(aBasicOrchestrationWorkflow().build())
                        .build());

    Pipeline pipeline = pipelineService.readPipeline(APP_ID, PIPELINE_ID, true);
    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));
    assertThat(pipeline.getDeploymentTypes()).hasSize(1).contains(DeploymentType.SSH);
    assertThat(pipeline.getEnvSummaries()).hasSize(1).extracting("uuid").isEqualTo(asList(ENV_ID));
    assertThat(pipeline.getEnvSummaries())
        .hasSize(1)
        .extracting("environmentType")
        .isEqualTo(asList(EnvironmentType.PROD));

    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldReadPipelineWithDetails() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", "BUILD_WORKFLOW_ID");
    PipelineStage pipelineStage2 =
        PipelineStage.builder()
            .pipelineStageElements(asList(PipelineStageElement.builder()
                                              .workflowVariables(ImmutableMap.of(ENV_NAME, ENV_ID, SERVICE_NAME,
                                                  SERVICE_ID, INFRA_NAME, INFRA_MAPPING_ID, "MyVar", ""))
                                              .name("STAGE1")
                                              .type(ENV_STATE.name())
                                              .properties(properties)
                                              .build()))
            .build();

    Pipeline pipeline2 = Pipeline.builder()
                             .name("pipeline2")
                             .appId(APP_ID)
                             .uuid(PIPELINE_ID)
                             .pipelineStages(asList(pipelineStage2,
                                 PipelineStage.builder()
                                     .pipelineStageElements(asList(PipelineStageElement
                                                                       .builder()

                                                                       .name("STAGE2")
                                                                       .type(ENV_STATE.name())
                                                                       .properties(properties)
                                                                       .build()))
                                     .build()))
                             .build();

    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID)).thenReturn(pipeline2);

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean()))
        .thenReturn(aWorkflow()
                        .name(WORKFLOW_NAME)
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .orchestrationWorkflow(
                            aBasicOrchestrationWorkflow()
                                .withUserVariables(asList(aVariable().name(ENV_NAME).entityType(ENVIRONMENT).build(),
                                    aVariable().name("MyVar").build()))
                                .build())
                        .build());

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq("BUILD_WORKFLOW_ID"), anyBoolean()))
        .thenReturn(aWorkflow()
                        .name(WORKFLOW_NAME)
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .orchestrationWorkflow(
                            aBuildOrchestrationWorkflow()
                                .withUserVariables(asList(aVariable().name(ENV_NAME).entityType(ENVIRONMENT).build(),
                                    aVariable().name("MyVar").build()))
                                .build())
                        .build());

    Pipeline pipeline = pipelineService.readPipeline(APP_ID, PIPELINE_ID, true);
    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));
    assertThat(pipeline.getDeploymentTypes()).hasSize(1).contains(DeploymentType.SSH);
    assertThat(pipeline.getEnvSummaries()).hasSize(1).extracting("uuid").isEqualTo(asList(ENV_ID));
    assertThat(pipeline.getEnvSummaries())
        .hasSize(1)
        .extracting("environmentType")
        .isEqualTo(asList(EnvironmentType.PROD));
    assertThat(pipeline.isValid()).isFalse();
    assertThat(pipeline.getValidationMessage()).isNotEmpty();
    assertThat(pipeline.getPipelineVariables()).hasSize(1).extracting(Variable::getName).contains("MyVar");
    assertThat(pipeline.isHasBuildWorkflow()).isTrue();

    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetPipelineWithServicesAndEnvs() {
    mockPipeline();

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                                   .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                                   .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                                   .build())
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .build());

    when(workflowService.resolveEnvironmentId(any(), any())).thenReturn(ENV_ID);
    when(workflowService.getResolvedServices(any(), anyMap()))
        .thenReturn(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()));

    Pipeline pipeline = pipelineService.readPipelineWithResolvedVariables(APP_ID, PIPELINE_ID, null);
    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));
    assertThat(pipeline.getEnvIds()).hasSize(1).contains(ENV_ID);
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldNotIncludeDisableStepServices() {
    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID))
        .thenReturn(
            Pipeline.builder()
                .appId(APP_ID)
                .uuid(PIPELINE_ID)
                .pipelineStages(asList(
                    PipelineStage.builder()
                        .pipelineStageElements(
                            asList(PipelineStageElement.builder()
                                       .name("SE")
                                       .type(ENV_STATE.name())
                                       .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", WORKFLOW_ID))
                                       .build(),
                                PipelineStageElement.builder()
                                    .name("SE")
                                    .type(ENV_STATE.name())
                                    .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", "DISABLE_WORKFLOW_ID"))
                                    .disable(true)
                                    .build()))
                        .build()))
                .build());

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean()))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                                   .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                                   .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                                   .build())
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .build());

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq("DISABLE_WORKFLOW_ID"), anyBoolean()))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                                   .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                                   .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                                   .build())
                        .services(asList(Service.builder()
                                             .appId(APP_ID)
                                             .uuid("DISABLE_STEP_SERVICE_ID")
                                             .name("DISABLE_STEP_SERVICE_NAME")
                                             .build()))
                        .build());

    Pipeline pipeline = pipelineService.readPipeline(APP_ID, PIPELINE_ID, true);
    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").doesNotContain(asList("DISABLE_STEP_SERVICE_ID"));
    assertThat(pipeline.getEnvIds()).hasSize(0).doesNotContain(ENV_ID);
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetPipelineWithTemplatizedServices() {
    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID))
        .thenReturn(
            Pipeline.builder()
                .appId(APP_ID)
                .uuid(PIPELINE_ID)
                .pipelineStages(
                    asList(PipelineStage.builder()
                               .pipelineStageElements(asList(
                                   PipelineStageElement.builder()
                                       .name("SE")
                                       .type(ENV_STATE.name())
                                       .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", WORKFLOW_ID))
                                       .workflowVariables(ImmutableMap.of("Environment", ENV_ID, "Service", SERVICE_ID))
                                       .build()))
                               .build()))
                .build());

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean()))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(
                            aCanaryOrchestrationWorkflow()
                                .withUserVariables(
                                    asList(aVariable().entityType(SERVICE).name("Service").value(SERVICE_ID).build()))
                                .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                .build())
                        .build());

    Pipeline pipeline = pipelineService.readPipeline(APP_ID, PIPELINE_ID, true);

    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));

    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldReadPipelineWithNoPipelineVariables() {
    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID))
        .thenReturn(
            Pipeline.builder()
                .appId(APP_ID)
                .uuid(PIPELINE_ID)
                .pipelineStages(
                    asList(PipelineStage.builder()
                               .pipelineStageElements(asList(
                                   PipelineStageElement.builder()
                                       .name("SE")
                                       .type(ENV_STATE.name())
                                       .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", WORKFLOW_ID))
                                       .workflowVariables(ImmutableMap.of("Environment", ENV_ID, "Service", SERVICE_ID))
                                       .build()))
                               .build()))
                .build());

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(
                            aCanaryOrchestrationWorkflow()
                                .withUserVariables(
                                    asList(aVariable().entityType(SERVICE).name("Service").value(SERVICE_ID).build()))
                                .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                .build())
                        .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                        .build());

    when(workflowService.getResolvedServices(any(Workflow.class), any()))
        .thenReturn(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()));
    when(workflowService.resolveEnvironmentId(any(), any())).thenReturn(ENV_ID);

    Pipeline pipeline = pipelineService.readPipelineWithResolvedVariables(APP_ID, PIPELINE_ID, null);

    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));

    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldReadPipelineWithResolvedPipelineVariables() {
    Pipeline pipelineWithVariables =
        Pipeline.builder()
            .appId(APP_ID)
            .uuid(PIPELINE_ID)
            .pipelineStages(
                asList(PipelineStage.builder()
                           .pipelineStageElements(
                               asList(PipelineStageElement.builder()
                                          .name("SE")
                                          .type(ENV_STATE.name())
                                          .properties(ImmutableMap.of("envId", "${ENV}", "workflowId", WORKFLOW_ID))
                                          .workflowVariables(ImmutableMap.of("Environment", "${ENV}", "Service",
                                              "${SERVICE}", "ServiceInfrastructure_SSH", "${INFRA}"))
                                          .build()))
                           .build()))
            .build();

    pipelineWithVariables.getPipelineVariables().add(aVariable().entityType(ENVIRONMENT).name("ENV").build());
    pipelineWithVariables.getPipelineVariables().add(aVariable().entityType(SERVICE).name("SERVICE").build());
    pipelineWithVariables.getPipelineVariables().add(
        aVariable().entityType(INFRASTRUCTURE_MAPPING).name("INFRA").build());

    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID)).thenReturn(pipelineWithVariables);

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(
            aWorkflow()
                .orchestrationWorkflow(
                    aCanaryOrchestrationWorkflow()
                        .withUserVariables(asList(aVariable().entityType(SERVICE).name("Service").build(),
                            aVariable().entityType(ENVIRONMENT).name("Environment").build(),
                            aVariable().entityType(INFRASTRUCTURE_MAPPING).name("ServiceInfrastructure_SSH").build()))
                        .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                        .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                        .build())
                .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
                .build());

    when(workflowService.getResolvedServices(any(Workflow.class), any()))
        .thenReturn(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()));
    when(workflowService.resolveEnvironmentId(any(), any())).thenReturn(ENV_ID);

    Pipeline pipeline = pipelineService.readPipelineWithResolvedVariables(APP_ID, PIPELINE_ID,
        ImmutableMap.of("ENV", ENV_ID, "SERVICE", SERVICE_ID, "INFRA", INFRA_MAPPING_ID, "MyVar", "MyValue"));

    assertThat(pipeline).isNotNull().hasFieldOrPropertyWithValue("uuid", PIPELINE_ID);
    assertThat(pipeline.getServices()).hasSize(1).extracting("uuid").isEqualTo(asList(SERVICE_ID));

    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldDeletePipeline() {
    when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
        .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));

    mockPipeline();
    when(wingsPersistence.delete(Pipeline.class, APP_ID, PIPELINE_ID)).thenReturn(true);

    assertThat(pipelineService.deletePipeline(APP_ID, PIPELINE_ID)).isTrue();
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void deletePipelineExecutionInProgress() {
    mockPipeline();
    PipelineExecution pipelineExecution = aPipelineExecution().withStatus(ExecutionStatus.RUNNING).build();
    PageResponse pageResponse = aPageResponse().withResponse(asList(pipelineExecution)).build();
    when(wingsPersistence.query(PipelineExecution.class,
             aPageRequest().addFilter("appId", EQ, APP_ID).addFilter("pipelineId", EQ, PIPELINE_ID).build()))
        .thenReturn(pageResponse);
    pipelineService.deletePipeline(APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldPruneDescendingObjects() {
    pipelineService.pruneDescendingEntities(APP_ID, PIPELINE_ID);
    InOrder inOrder = inOrder(wingsPersistence, workflowService, triggerService);
    inOrder.verify(triggerService).pruneByPipeline(APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldCheckIfEnvReferenced() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage =
        PipelineStage.builder()
            .pipelineStageElements(asList(
                PipelineStageElement.builder().name("STAGE1").type(ENV_STATE.name()).properties(properties).build(),
                PipelineStageElement.builder()
                    .name("STAGE2")
                    .type(ENV_STATE.name())
                    .properties(properties)
                    .workflowVariables(
                        ImmutableMap.of("Environment", ENV_ID, "Service", SERVICE_ID, "ServiceInfra", INFRA_MAPPING_ID))
                    .build()))
            .build();

    List<PipelineStage> pipelineStages = new ArrayList<>();
    pipelineStages.add(pipelineStage);

    Pipeline pipeline =
        Pipeline.builder().name("pipeline1").appId(APP_ID).pipelineStages(pipelineStages).uuid(PIPELINE_ID).build();

    when(wingsPersistence.createQuery(eq(Pipeline.class))).thenReturn(pquery);

    when(pquery.field(any())).thenReturn(end);
    when(end.in(any())).thenReturn(pquery);
    when(pquery.filter(any(), any())).thenReturn(pquery);
    when(wingsPersistence.createQuery(Pipeline.class).filter(any(), any()).get()).thenReturn(pipeline);

    when(wingsPersistence.saveAndGet(eq(Pipeline.class), eq(pipeline))).thenReturn(pipeline);

    when(pquery.fetch()).thenReturn(pipelineIterator);

    when(pipelineIterator.hasNext()).thenReturn(true).thenReturn(false);

    when(pipelineIterator.next()).thenReturn(pipeline);

    when(wingsPersistence.getWithAppId(Workflow.class, APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow().envId(ENV_ID).build());

    List<String> refPipelines = pipelineService.obtainPipelineNamesReferencedByEnvironment(APP_ID, ENV_ID);
    assertThat(!refPipelines.isEmpty() && refPipelines.size() > 0).isTrue();
  }

  @Test
  @Category(UnitTests.class)
  public void shouldCheckTemplatedEntityReferenced() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage = prepareTemplatedStage(properties);

    List<PipelineStage> pipelineStages = new ArrayList<>();
    pipelineStages.add(pipelineStage);

    Pipeline pipeline =
        Pipeline.builder().name("pipeline1").appId(APP_ID).pipelineStages(pipelineStages).uuid(PIPELINE_ID).build();

    when(wingsPersistence.createQuery(eq(Pipeline.class))).thenReturn(pquery);

    when(pquery.field(any())).thenReturn(end);
    when(end.in(any())).thenReturn(pquery);
    when(pquery.filter(any(), any())).thenReturn(pquery);
    when(wingsPersistence.createQuery(Pipeline.class).filter(any(), any()).get()).thenReturn(pipeline);

    when(wingsPersistence.saveAndGet(eq(Pipeline.class), eq(pipeline))).thenReturn(pipeline);

    when(pquery.fetch()).thenReturn(pipelineIterator);

    when(pipelineIterator.hasNext()).thenReturn(true).thenReturn(false);

    when(pipelineIterator.next()).thenReturn(pipeline);

    assertThat(pipelineService.obtainPipelineNamesReferencedByTemplatedEntity(APP_ID, ENV_ID)).isNotEmpty();
    when(pipelineIterator.hasNext()).thenReturn(true).thenReturn(false);

    when(pipelineIterator.next()).thenReturn(pipeline);
    assertThat(pipelineService.obtainPipelineNamesReferencedByTemplatedEntity(APP_ID, SERVICE_ID)).isNotEmpty();
    when(pipelineIterator.hasNext()).thenReturn(true).thenReturn(false);

    when(pipelineIterator.next()).thenReturn(pipeline);
    assertThat(pipelineService.obtainPipelineNamesReferencedByTemplatedEntity(APP_ID, INFRA_MAPPING_ID)).isNotEmpty();
  }

  @Test
  @Category(UnitTests.class)
  public void shouldListPipelines() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage =
        PipelineStage.builder()
            .pipelineStageElements(asList(
                PipelineStageElement.builder().name("STAGE1").type(ENV_STATE.name()).properties(properties).build()))
            .build();

    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))
                            .failureStrategies(asList(failureStrategy))
                            .build();

    Map<String, Object> properties2 = new HashMap<>();
    properties2.put("envId", ENV_ID);
    properties2.put("workflowId", WORKFLOW_ID);

    Pipeline pipeline2 = Pipeline.builder()
                             .name("pipeline2")
                             .appId(APP_ID)
                             .uuid(PIPELINE_ID)
                             .pipelineStages(asList(prepareTemplatedStage(properties)))
                             .build();
    when(wingsPersistence.query(Pipeline.class, aPageRequest().build()))
        .thenReturn(aPageResponse().withResponse(asList(pipeline, pipeline2)).build());

    PageResponse pageResponse = pipelineService.listPipelines(aPageRequest().build());
    List<Pipeline> pipelines = pageResponse.getResponse();
    assertThat(pipelines).isNotEmpty().size().isEqualTo(2);
    assertThat(pipelines.get(0).getName()).isEqualTo("pipeline1");
    assertThat(pipelines.get(0).getAppId()).isEqualTo(APP_ID);
    assertThat(pipelines.get(1).getName()).isEqualTo("pipeline2");
    assertThat(pipelines.get(1).getAppId()).isEqualTo(APP_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldListPipelinesWithDetails() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage =
        PipelineStage.builder()
            .pipelineStageElements(asList(
                PipelineStageElement.builder().name("STAGE1").type(ENV_STATE.name()).properties(properties).build()))
            .build();

    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))

                            .build();

    Map<String, Object> properties2 = new HashMap<>();
    properties2.put("envId", ENV_ID);
    properties2.put("workflowId", WORKFLOW_ID);

    Pipeline pipeline2 = Pipeline.builder()
                             .name("pipeline2")
                             .appId(APP_ID)
                             .uuid(PIPELINE_ID)
                             .pipelineStages(asList(prepareTemplatedStage(properties)))
                             .build();
    when(wingsPersistence.query(Pipeline.class, aPageRequest().build()))
        .thenReturn(aPageResponse().withResponse(asList(pipeline, pipeline2)).build());

    Workflow workflow = aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean())).thenReturn(workflow);
    PageRequest<WorkflowExecution> workflowExecutionPageRequest = aPageRequest()
                                                                      .withLimit("2")
                                                                      .addFilter("workflowId", EQ, pipeline.getUuid())
                                                                      .addFilter("appId", EQ, pipeline.getAppId())
                                                                      .build();
    when(workflowExecutionService.listExecutions(workflowExecutionPageRequest, false, false, false, false))
        .thenReturn(aPageResponse().build());

    PageResponse pageResponse = pipelineService.listPipelines(aPageRequest().build(), true, 2, false, null);
    List<Pipeline> pipelines = pageResponse.getResponse();
    assertThat(pipelines).isNotEmpty().size().isEqualTo(2);
    assertThat(pipelines.get(0).getName()).isEqualTo("pipeline1");
    assertThat(pipelines.get(0).getAppId()).isEqualTo(APP_ID);
    assertThat(pipelines.get(0).isValid()).isEqualTo(true);
    assertThat(pipelines.get(1).getName()).isEqualTo("pipeline2");
    assertThat(pipelines.get(1).isValid()).isEqualTo(false);
    assertThat(pipelines.get(1).getValidationMessage()).isNotEmpty();
    assertThat(pipelines.get(1).isHasSshInfraMapping()).isEqualTo(false);
    verify(workflowExecutionService, times(2)).listExecutions(workflowExecutionPageRequest, false, false, false, false);
  }

  private PipelineStage prepareTemplatedStage(Map<String, Object> properties) {
    return PipelineStage.builder()
        .pipelineStageElements(asList(PipelineStageElement.builder()
                                          .workflowVariables(ImmutableMap.of("Environment", ENV_ID, "Service",
                                              SERVICE_ID, "ServiceInfra", INFRA_MAPPING_ID))
                                          .name("STAGE1")
                                          .type(ENV_STATE.name())
                                          .properties(properties)
                                          .build()))
        .build();
  }

  @Test
  @Category(UnitTests.class)
  public void shouldListPipelinesWithVariables() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage =
        PipelineStage.builder()
            .pipelineStageElements(asList(
                PipelineStageElement.builder().name("STAGE1").type(ENV_STATE.name()).properties(properties).build()))
            .build();

    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))
                            .failureStrategies(asList(failureStrategy))
                            .build();

    Map<String, Object> properties2 = new HashMap<>();
    properties2.put("envId", ENV_ID);
    properties2.put("workflowId", WORKFLOW_ID);
    PipelineStage pipelineStage2 =
        PipelineStage.builder()
            .pipelineStageElements(asList(PipelineStageElement.builder()
                                              .workflowVariables(ImmutableMap.of(ENV_NAME, ENV_ID, SERVICE_NAME,
                                                  SERVICE_ID, INFRA_NAME, INFRA_MAPPING_ID))
                                              .name("STAGE1")
                                              .type(ENV_STATE.name())
                                              .properties(properties)
                                              .build()))
            .build();

    Pipeline pipeline2 = Pipeline.builder()
                             .name("pipeline2")
                             .appId(APP_ID)
                             .uuid(PIPELINE_ID)
                             .pipelineStages(asList(pipelineStage2))
                             .build();
    when(wingsPersistence.query(Pipeline.class, aPageRequest().build()))
        .thenReturn(aPageResponse().withResponse(asList(pipeline, pipeline2)).build());

    List<Variable> variables = Arrays.asList(aVariable().name("MyVar1").build(),
        aVariable().name("MyFixedVar").fixed(true).build(), aVariable().entityType(SERVICE).name(SERVICE_NAME).build(),
        aVariable().entityType(ENVIRONMENT).name(ENV_NAME).build(),
        aVariable().entityType(INFRASTRUCTURE_MAPPING).name(INFRA_NAME).build());

    Workflow workflow =
        aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().withUserVariables(variables).build()).build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean())).thenReturn(workflow);

    when(workflowExecutionService.listExecutions(Mockito.any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
        .thenReturn(aPageResponse().withResponse(new ArrayList()).build());

    PageResponse pageResponse = pipelineService.listPipelines(aPageRequest().build(), true, 2, false, null);
    List<Pipeline> pipelines = pageResponse.getResponse();
    assertThat(pipelines).isNotEmpty().size().isEqualTo(2);
    assertThat(pipelines.get(0).getName()).isEqualTo("pipeline1");
    assertThat(pipelines.get(0).getPipelineVariables()).isNotEmpty().extracting(Variable::getName).contains("MyVar1");
    assertThat(pipelines.get(0).getPipelineVariables())
        .isNotEmpty()
        .extracting(Variable::getName)
        .doesNotContain("MyFixedVar");
    assertThat(pipelines.get(0).getPipelineVariables())
        .isNotEmpty()
        .extracting(Variable::getName)
        .doesNotContain(SERVICE_NAME, ENV_NAME, INFRA_NAME);
    assertThat(pipelines.get(1).getName()).isEqualTo("pipeline2");
    assertThat(pipelines.get(1).getPipelineVariables()).isNotEmpty().extracting(Variable::getName).contains("MyVar1");
    assertThat(pipelines.get(1).getPipelineVariables())
        .isNotEmpty()
        .extracting(Variable::getName)
        .doesNotContain("MyFixedVar");
    assertThat(pipelines.get(1).getPipelineVariables())
        .isNotEmpty()
        .extracting(Variable::getName)
        .doesNotContain(SERVICE_NAME, ENV_NAME, INFRA_NAME);
    assertThat(pipelines.get(1).isHasSshInfraMapping()).isEqualTo(false);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldListPipelinesWithDetailsWithSshInfraMapping() {
    PipelineStage pipelineStage = prepareStageSimple();

    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))

                            .build();

    when(wingsPersistence.query(Pipeline.class, aPageRequest().build()))
        .thenReturn(aPageResponse().withResponse(asList(pipeline)).build());

    Workflow workflow =
        aWorkflow()
            .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                       .addWorkflowPhase(aWorkflowPhase().deploymentType(DeploymentType.SSH).build())
                                       .build())
            .build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean())).thenReturn(workflow);

    PageRequest<WorkflowExecution> workflowExecutionPageRequest = aPageRequest()
                                                                      .withLimit("2")
                                                                      .addFilter("workflowId", EQ, pipeline.getUuid())
                                                                      .addFilter("appId", EQ, pipeline.getAppId())
                                                                      .build();
    when(workflowExecutionService.listExecutions(workflowExecutionPageRequest, false, false, false, false))
        .thenReturn(aPageResponse().build());

    PageResponse pageResponse = pipelineService.listPipelines(aPageRequest().build(), true, 2, false, null);
    List<Pipeline> pipelines = pageResponse.getResponse();
    assertThat(pipelines).isNotEmpty().size().isEqualTo(1);
    assertThat(pipelines.get(0).getName()).isEqualTo("pipeline1");
    assertThat(pipelines.get(0).isValid()).isEqualTo(true);
    assertThat(pipelines.get(0).isHasSshInfraMapping()).isEqualTo(true);

    verify(workflowExecutionService, times(1)).listExecutions(workflowExecutionPageRequest, false, false, false, false);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetRequiredEntities() {
    mockPipeline();

    Workflow workflow =
        aWorkflow()
            .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                       .withUserVariables(asList(
                                           aVariable().entityType(SERVICE).name("Service").value(SERVICE_ID).build()))
                                       .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                       .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                       .build())
            .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
            .build();

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean())).thenReturn(workflow);
    when(workflowService.fetchDeploymentMetadata(APP_ID, workflow, null, null, null, Include.ARTIFACT_SERVICE))
        .thenReturn(DeploymentMetadata.builder().artifactRequiredServiceIds(asList(SERVICE_ID)).build());
    List<EntityType> requiredEntities = pipelineService.getRequiredEntities(APP_ID, PIPELINE_ID);
    assertThat(requiredEntities).isNotEmpty().contains(ARTIFACT);

    verify(workflowService).readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean());
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldGetRequiredEntitiesForBuildPipeline() {
    mockPipeline();

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean()))
        .thenReturn(aWorkflow()
                        .orchestrationWorkflow(aBuildOrchestrationWorkflow()
                                                   .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                                   .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                                   .build())
                        .build());
    List<EntityType> requiredEntities = pipelineService.getRequiredEntities(APP_ID, PIPELINE_ID);
    assertThat(requiredEntities).isEmpty();

    verify(workflowService).readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean());
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void shouldNotCreatePipelineWhenLimitExceeds() {
    when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
        .thenReturn(new MockChecker(false, ActionType.CREATE_PIPELINE));

    PipelineStage pipelineStage = prepareStageSimple();

    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    Pipeline pipeline = Pipeline.builder()
                            .name("pipeline1")
                            .appId(APP_ID)
                            .uuid(PIPELINE_ID)
                            .pipelineStages(asList(pipelineStage))
                            .failureStrategies(asList(failureStrategy))
                            .build();

    when(wingsPersistence.saveAndGet(eq(Pipeline.class), eq(pipeline))).thenReturn(pipeline);
    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
        .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());
    when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

    try {
      pipelineService.save(pipeline);
    } catch (WingsException e) {
      assertThat(e.getCode()).isEqualByComparingTo(ErrorCode.USAGE_LIMITS_EXCEEDED);
      throw e;
    }
  }

  @Test
  @Category(UnitTests.class)
  public void shouldNotIncludeRequiredEntitiesForDisabledStep() {
    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID))
        .thenReturn(Pipeline.builder()
                        .appId(APP_ID)
                        .uuid(PIPELINE_ID)
                        .pipelineStages(
                            asList(PipelineStage.builder()
                                       .pipelineStageElements(asList(
                                           PipelineStageElement.builder()
                                               .name("SE")
                                               .type(ENV_STATE.name())
                                               .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", WORKFLOW_ID))
                                               .disableAssertion("true")
                                               .build()))
                                       .build()))
                        .build());

    Workflow workflow =
        aWorkflow()
            .orchestrationWorkflow(aCanaryOrchestrationWorkflow()
                                       .withUserVariables(asList(
                                           aVariable().entityType(SERVICE).name("Service").value(SERVICE_ID).build()))
                                       .withPreDeploymentSteps(aPhaseStep(PRE_DEPLOYMENT).build())
                                       .withPostDeploymentSteps(aPhaseStep(POST_DEPLOYMENT).build())
                                       .withRequiredEntityTypes(ImmutableSet.of(ARTIFACT, SSH_USER, SSH_PASSWORD))
                                       .build())
            .services(asList(Service.builder().appId(APP_ID).uuid(SERVICE_ID).name(SERVICE_NAME).build()))
            .build();

    when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID)).thenReturn(workflow);
    when(workflowService.fetchRequiredEntityTypes(APP_ID, workflow))
        .thenReturn(ImmutableSet.of(ARTIFACT, SSH_USER, SSH_PASSWORD));
    List<EntityType> requiredEntities = pipelineService.getRequiredEntities(APP_ID, PIPELINE_ID);
    assertThat(requiredEntities).isEmpty();

    verify(workflowService, times(0)).readWorkflow(APP_ID, WORKFLOW_ID);
    verify(wingsPersistence).getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID);
  }

  private void mockPipeline() {
    when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, PIPELINE_ID))
        .thenReturn(Pipeline.builder()
                        .appId(APP_ID)
                        .uuid(PIPELINE_ID)
                        .pipelineStages(
                            asList(PipelineStage.builder()
                                       .pipelineStageElements(asList(
                                           PipelineStageElement.builder()
                                               .name("SE")
                                               .type(ENV_STATE.name())
                                               .properties(ImmutableMap.of("envId", ENV_ID, "workflowId", WORKFLOW_ID))
                                               .build()))
                                       .build()))
                        .build());
  }

  @Test
  @Category(UnitTests.class)
  public void testCreatePipelineWithSameName() {
    try {
      when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
          .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));
      when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
          .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());
      when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

      Pipeline pipeline =
          pipelineService.save(Pipeline.builder().name("pipeline").appId(APP_ID).uuid(PIPELINE_ID).build());
      assertThat(pipeline).isNotNull().hasFieldOrProperty("uuid");
      when(pipelineQuery.project(anyString(), anyBoolean()).getKey())
          .thenReturn(new Key<>(Pipeline.class, "pipelines", PIPELINE_ID));
      pipeline.setUuid(null);
      pipelineService.save(Pipeline.builder().name("pipeline").appId(APP_ID).uuid(PIPELINE_ID).build());
      fail("Expected an InvalidRequestException to be thrown");
    } catch (InvalidRequestException exception) {
      assertThat(exception.getParams().get("message")).isEqualTo("Duplicate name pipeline");
    }
  }
  @Test
  @Category(UnitTests.class)
  public void testClonePipelineWithSameName() {
    try {
      when(limitCheckerFactory.getInstance(new Action(Mockito.anyString(), ActionType.CREATE_PIPELINE)))
          .thenReturn(new MockChecker(true, ActionType.CREATE_PIPELINE));
      when(workflowService.readWorkflow(APP_ID, WORKFLOW_ID))
          .thenReturn(aWorkflow().orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build());
      when(workflowService.stencilMap(any())).thenReturn(ImmutableMap.of("ENV_STATE", StateType.ENV_STATE));

      Pipeline pipeline =
          pipelineService.save(Pipeline.builder().name("pipeline").appId(APP_ID).uuid(PIPELINE_ID).build());
      assertThat(pipeline).isNotNull().hasFieldOrProperty("uuid");

      when(pipelineQuery.project(anyString(), anyBoolean()).getKey())
          .thenReturn(new Key<>(Pipeline.class, "pipelines", PIPELINE_ID));
      pipelineService.clonePipeline(
          pipeline, Pipeline.builder().name("pipeline").appId(APP_ID).uuid(PIPELINE_ID).build());
      fail("Expected an InvalidRequestException to be thrown");
    } catch (InvalidRequestException exception) {
      assertThat(exception.getParams().get("message")).isEqualTo("Duplicate name pipeline");
    }
  }

  @Test
  @Category(UnitTests.class)
  public void shouldFetchDeploymentMetadata() {
    validateFetchDeploymentMetadata(false, false);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldFetchDeploymentMetadataWithId() {
    validateFetchDeploymentMetadata(true, false);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldFetchDeploymentMetadataWithIdAndDefaultArtifact() {
    validateFetchDeploymentMetadata(true, true);
  }

  private void validateFetchDeploymentMetadata(boolean withId, boolean withDefaultArtifact) {
    String workflowId1 = WORKFLOW_ID + "_1";
    String workflowId2 = WORKFLOW_ID + "_2";
    String workflowId3 = WORKFLOW_ID + "_3";
    Pipeline pipeline = preparePipeline(prepareStageSimple(), prepareStageSimple(ENV_ID, workflowId1),
        prepareStageSimple(), prepareStageDisabled(), prepareStageApproval(), prepareStageSimple(ENV_ID, workflowId2),
        prepareStageSimple(ENV_ID, workflowId3));
    List<String> artifactNeededServiceIds = new ArrayList<>();
    List<String> envIds = new ArrayList<>();

    Workflow workflow =
        aWorkflow().uuid(WORKFLOW_ID).name("w").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    Workflow workflow1 =
        aWorkflow().uuid(workflowId1).name("w1").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    Workflow workflow2 =
        aWorkflow().uuid(workflowId2).name("w2").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    Workflow workflow3 =
        aWorkflow().uuid(workflowId3).name("w3").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();

    Map<String, Workflow> workflowMap = new HashMap<>();
    workflowMap.put(WORKFLOW_ID, workflow);
    workflowMap.put(workflowId1, workflow1);
    workflowMap.put(workflowId2, workflow2);
    workflowMap.put(workflowId3, workflow3);

    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), anyString(), anyBoolean()))
        .thenAnswer(invocation -> workflowMap.getOrDefault((String) invocation.getArguments()[1], null));

    when(workflowService.readWorkflow(eq(APP_ID), anyString()))
        .thenAnswer(invocation -> workflowMap.getOrDefault((String) invocation.getArguments()[1], null));

    when(workflowService.fetchDeploymentMetadata(
             any(), any(Workflow.class), anyMap(), any(), any(), anyBoolean(), any(), anyVararg()))
        .thenAnswer(invocation -> {
          Workflow argument = (Workflow) invocation.getArguments()[1];
          switch (argument.getName()) {
            case "w":
              return DeploymentMetadata.builder()
                  .artifactRequiredServiceIds(Collections.singletonList("s1"))
                  .artifactVariables(Collections.singletonList(prepareArtifactVariable("artifact", "s1")))
                  .build();
            case "w1":
              return DeploymentMetadata.builder().build();
            case "w2":
              return DeploymentMetadata.builder()
                  .artifactRequiredServiceIds(asList("s1", "s2"))
                  .artifactVariables(asList(prepareArtifactVariable("artifact", "s1"),
                      prepareArtifactVariable("artifact", "s2"), prepareArtifactVariable("artifact_tmp", "s2"), null))
                  .build();
            default:
              return null;
          }
        });

    DeploymentMetadata deploymentMetadata;
    if (withId) {
      when(wingsPersistence.getWithAppId(Pipeline.class, APP_ID, pipeline.getUuid())).thenReturn(pipeline);
      when(workflowService.getResolvedServices(any(), any())).thenReturn(Collections.emptyList());
      when(workflowService.getResolvedInfraMappingIds(any(), any())).thenReturn(Collections.emptyList());
      when(workflowService.getResolvedInfraDefinitionIds(any(), any())).thenReturn(Collections.emptyList());
      when(workflowService.resolveEnvironmentId(any(), any())).thenReturn(null);
      if (withDefaultArtifact) {
        deploymentMetadata = pipelineService.fetchDeploymentMetadata(
            APP_ID, pipeline.getUuid(), null, artifactNeededServiceIds, envIds, false, null);
      } else {
        deploymentMetadata =
            pipelineService.fetchDeploymentMetadata(APP_ID, pipeline.getUuid(), null, artifactNeededServiceIds, envIds);
      }
    } else {
      deploymentMetadata = pipelineService.fetchDeploymentMetadata(APP_ID, pipeline, artifactNeededServiceIds, envIds);
    }

    assertThat(deploymentMetadata).isNotNull();

    List<String> serviceIds = deploymentMetadata.getArtifactRequiredServiceIds();
    assertThat(serviceIds).isNotNull();
    assertThat(serviceIds.size()).isEqualTo(2);
    assertThat(serviceIds).contains("s1", "s2");

    List<ArtifactVariable> artifactVariables = deploymentMetadata.getArtifactVariables();
    assertThat(artifactVariables).isNotNull();
    assertThat(artifactVariables.size()).isEqualTo(3);

    ArtifactVariable artifactVariable1 =
        artifactVariables.stream()
            .filter(artifactVariable
                -> artifactVariable.getName().equals("artifact") && artifactVariable.getEntityId().equals("s1"))
            .findFirst()
            .orElse(null);
    assertThat(artifactVariable1).isNotNull();
    assertThat(artifactVariable1.getWorkflowIds()).isNotNull();
    assertThat(artifactVariable1.getWorkflowIds().size()).isEqualTo(2);
    assertThat(artifactVariable1.getWorkflowIds()).contains(WORKFLOW_ID, workflowId2);

    ArtifactVariable artifactVariable2 =
        artifactVariables.stream()
            .filter(artifactVariable
                -> artifactVariable.getName().equals("artifact") && artifactVariable.getEntityId().equals("s2"))
            .findFirst()
            .orElse(null);
    assertThat(artifactVariable2).isNotNull();
    assertThat(artifactVariable2.getWorkflowIds()).isNotNull();
    assertThat(artifactVariable2.getWorkflowIds().size()).isEqualTo(1);
    assertThat(artifactVariable2.getWorkflowIds()).contains(workflowId2);

    ArtifactVariable artifactVariable3 =
        artifactVariables.stream()
            .filter(artifactVariable -> artifactVariable.getName().equals("artifact_tmp"))
            .findFirst()
            .orElse(null);
    assertThat(artifactVariable3).isNotNull();
    assertThat(artifactVariable3.getWorkflowIds()).isNotNull();
    assertThat(artifactVariable3.getWorkflowIds().size()).isEqualTo(1);
    assertThat(artifactVariable3.getWorkflowIds()).contains(workflowId2);
  }

  @Test
  @Category(UnitTests.class)
  public void shouldFetchDeploymentMetadataForBuildPipeline() {
    String workflowId0 = WORKFLOW_ID + "_0";
    String workflowId1 = WORKFLOW_ID + "_1";
    Pipeline pipeline = preparePipeline(
        prepareStageSimple(ENV_ID, workflowId0), prepareStageSimple(), prepareStageSimple(ENV_ID, workflowId1));

    Workflow workflow0 =
        aWorkflow().uuid(workflowId1).name("w0").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(workflowId0), anyBoolean())).thenReturn(workflow0);

    Workflow workflow =
        aWorkflow()
            .uuid(WORKFLOW_ID)
            .name("w")
            .orchestrationWorkflow(
                aCanaryOrchestrationWorkflow().withOrchestrationWorkflowType(OrchestrationWorkflowType.BUILD).build())
            .build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(WORKFLOW_ID), anyBoolean())).thenReturn(workflow);

    Workflow workflow1 =
        aWorkflow().uuid(workflowId1).name("w1").orchestrationWorkflow(aCanaryOrchestrationWorkflow().build()).build();
    when(workflowService.readWorkflowWithoutServices(eq(APP_ID), eq(workflowId1), anyBoolean())).thenReturn(workflow1);

    when(workflowService.fetchDeploymentMetadata(any(), any(Workflow.class), anyMap(), any(), any(), anyVararg()))
        .thenAnswer(invocation -> {
          Workflow argument = (Workflow) invocation.getArguments()[1];
          switch (argument.getName()) {
            case "w":
            case "w1":
              return DeploymentMetadata.builder().build();
            case "w0":
              return DeploymentMetadata.builder()
                  .artifactRequiredServiceIds(asList("s1", "s2"))
                  .artifactVariables(
                      asList(prepareArtifactVariable("artifact", "s1"), prepareArtifactVariable("artifact", "s2")))
                  .build();
            default:
              return null;
          }
        });

    DeploymentMetadata deploymentMetadata = pipelineService.fetchDeploymentMetadata(APP_ID, pipeline, null, null);
    assertThat(deploymentMetadata).isNotNull();
    assertThat(deploymentMetadata.getArtifactVariables()).isEmpty();
  }

  private ArtifactVariable prepareArtifactVariable(String name, String serviceId) {
    return ArtifactVariable.builder().name(name).entityType(SERVICE).entityId(serviceId).build();
  }

  private Pipeline preparePipeline(PipelineStage... pipelineStages) {
    FailureStrategy failureStrategy =
        FailureStrategy.builder().repairActionCode(RepairActionCode.MANUAL_INTERVENTION).build();
    return Pipeline.builder()
        .name("pipeline")
        .appId(APP_ID)
        .uuid(PIPELINE_ID)
        .pipelineStages(asList(pipelineStages))
        .failureStrategies(Collections.singletonList(failureStrategy))
        .build();
  }

  private PipelineStage prepareStageSimple() {
    return prepareStageSimple(ENV_ID, WORKFLOW_ID);
  }

  private PipelineStage prepareStageSimple(String envId, String workflowId) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", envId);
    properties.put("workflowId", workflowId);
    return PipelineStage.builder()
        .pipelineStageElements(Collections.singletonList(
            PipelineStageElement.builder().name("STAGE1").type(ENV_STATE.name()).properties(properties).build()))
        .build();
  }

  private PipelineStage prepareStageDisableAssertion(String envId, String workflowId) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", envId);
    properties.put("workflowId", workflowId);
    return PipelineStage.builder()
        .pipelineStageElements(Collections.singletonList(PipelineStageElement.builder()
                                                             .name("STAGE1")
                                                             .type(ENV_STATE.name())
                                                             .properties(properties)
                                                             .disableAssertion("true")
                                                             .build()))
        .build();
  }

  private PipelineStage prepareStageApprovalDisableAssertion() {
    return PipelineStage.builder()
        .pipelineStageElements(Collections.singletonList(PipelineStageElement.builder()
                                                             .name("STAGE2")
                                                             .type(APPROVAL.name())
                                                             .disableAssertion("random")
                                                             .properties(new HashMap<>())
                                                             .build()))
        .build();
  }

  private PipelineStage prepareStageDisabled() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("envId", ENV_ID);
    properties.put("workflowId", WORKFLOW_ID);
    return PipelineStage.builder()
        .pipelineStageElements(Collections.singletonList(PipelineStageElement.builder()
                                                             .name("STAGE1")
                                                             .type(ENV_STATE.name())
                                                             .disable(true)
                                                             .properties(properties)
                                                             .build()))
        .build();
  }

  private PipelineStage prepareStageApproval() {
    return PipelineStage.builder()
        .pipelineStageElements(Collections.singletonList(
            PipelineStageElement.builder().name("STAGE1").type(APPROVAL.name()).disable(false).build()))
        .build();
  }
}
