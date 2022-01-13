package software.wings.service.impl.workflow.creation;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.validation.Validator.notNullCheck;

import static software.wings.beans.Workflow.WorkflowBuilder.aWorkflow;
import static software.wings.common.WorkflowConstants.K8S_CANARY_PHASE_NAME;
import static software.wings.common.WorkflowConstants.K8S_PRIMARY_PHASE_NAME;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.ff.FeatureFlagService;
import io.harness.serializer.MapperUtils;

import software.wings.beans.CanaryOrchestrationWorkflow;
import software.wings.beans.OrchestrationWorkflow;
import software.wings.beans.Workflow;
import software.wings.beans.WorkflowPhase;
import software.wings.service.impl.workflow.WorkflowServiceHelper;
import software.wings.service.impl.workflow.WorkflowServiceTemplateHelper;
import software.wings.service.impl.workflow.creation.helpers.*;
import software.wings.service.intfc.InfrastructureDefinitionService;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(CDC)
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
@Slf4j
public class K8V2CanaryWorkflowCreator extends WorkflowCreator {
  private static final String PHASE_NAME = "CANARY";
  public static final String RANCHER_INFRA_TYPE = "RANCHER_KUBERNETES";
  @Inject private K8CanaryWorkflowPhaseHelper k8CanaryWorkflowPhaseHelper;
  @Inject private RancherK8CanaryWorkflowPhaseHelper rancherK8CanaryWorkflowPhaseHelper;
  @Inject private K8RollingWorkflowPhaseHelper k8RollingWorkflowPhaseHelper;
  @Inject private RancherK8RollingWorkflowPhaseHelper rancherK8RollingWorkflowPhaseHelper;
  @Inject private WorkflowServiceHelper workflowServiceHelper;
  @Inject private WorkflowServiceTemplateHelper workflowServiceTemplateHelper;
  @Inject private WorkflowPhaseHelper workflowPhaseHelper;
  @Inject private FeatureFlagService featureFlagService;
  @Inject private InfrastructureDefinitionService infrastructureDefinitionService;

  @Override
  public Workflow createWorkflow(Workflow clientWorkflow) {
    Workflow workflow = aWorkflow().build();
    updateCanaryWorkflowHelper(clientWorkflow.getAccountId(), clientWorkflow.getInfraDefinitionId());
    updateRollingWorkflowHelper(clientWorkflow.getAccountId(), clientWorkflow.getInfraDefinitionId());
    MapperUtils.mapObject(clientWorkflow, workflow);
    OrchestrationWorkflow orchestrationWorkflow = workflow.getOrchestrationWorkflow();
    CanaryOrchestrationWorkflow canaryOrchestrationWorkflow = (CanaryOrchestrationWorkflow) orchestrationWorkflow;
    notNullCheck("orchestrationWorkflow", canaryOrchestrationWorkflow);
    if (k8CanaryWorkflowPhaseHelper.isCreationRequired(canaryOrchestrationWorkflow)) {
      addLinkedPreOrPostDeploymentSteps(canaryOrchestrationWorkflow);
      addWorkflowPhases(workflow);
    }
    return workflow;
  }

  private void addWorkflowPhases(Workflow workflow) {
    CanaryOrchestrationWorkflow orchestrationWorkflow =
        (CanaryOrchestrationWorkflow) workflow.getOrchestrationWorkflow();
    WorkflowPhase workflowCanaryPhase = k8CanaryWorkflowPhaseHelper.getWorkflowPhase(workflow, PHASE_NAME);
    orchestrationWorkflow.getWorkflowPhases().add(workflowCanaryPhase);
    orchestrationWorkflow.getRollbackWorkflowPhaseIdMap().put(workflowCanaryPhase.getUuid(),
        k8CanaryWorkflowPhaseHelper.getRollbackPhaseForWorkflowPhase(workflowCanaryPhase));

    WorkflowPhase workflowRollingPhase = k8RollingWorkflowPhaseHelper.getWorkflowPhase(workflow, "Primary");
    orchestrationWorkflow.getWorkflowPhases().add(workflowRollingPhase);
    orchestrationWorkflow.getRollbackWorkflowPhaseIdMap().put(workflowRollingPhase.getUuid(),
        k8RollingWorkflowPhaseHelper.getRollbackPhaseForWorkflowPhase(workflowRollingPhase));
  }

  @Override
  public void attachWorkflowPhase(Workflow workflow, WorkflowPhase workflowPhase) {
    updateCanaryWorkflowHelper(workflow.getAccountId(), workflowPhase.getInfraDefinitionId());
    updateRollingWorkflowHelper(workflow.getAccountId(), workflowPhase.getInfraDefinitionId());

    workflowPhaseHelper.setCloudProviderIfNeeded(workflow, workflowPhase);
    boolean stepsGenerated = workflowPhaseHelper.addPhaseIfStepsGenerated(workflow, workflowPhase);
    if (stepsGenerated) {
      return;
    }

    OrchestrationWorkflow orchestrationWorkflow = workflow.getOrchestrationWorkflow();
    CanaryOrchestrationWorkflow canaryOrchestrationWorkflow = (CanaryOrchestrationWorkflow) orchestrationWorkflow;
    boolean serviceRepeat = canaryOrchestrationWorkflow.serviceRepeat(workflowPhase);
    boolean createCanaryPhase = !serviceRepeat;
    boolean createPrimaryPhase =
        serviceRepeat && !canaryOrchestrationWorkflow.containsPhaseWithName(K8S_PRIMARY_PHASE_NAME);

    if (createPrimaryPhase) {
      if (isBlank(workflowPhase.getName())) {
        workflowPhase.setName(K8S_PRIMARY_PHASE_NAME);
      }
      workflowPhase.getPhaseSteps().addAll(k8RollingWorkflowPhaseHelper.getWorkflowPhaseSteps());
    } else if (createCanaryPhase) {
      if (isBlank(workflowPhase.getName())) {
        workflowPhase.setName(K8S_CANARY_PHASE_NAME);
      }
      workflowPhase.getPhaseSteps().addAll(k8CanaryWorkflowPhaseHelper.getWorkflowPhaseSteps());
    } else {
      workflowPhaseHelper.addK8sEmptyPhaseStep(workflowPhase);
    }

    workflowServiceTemplateHelper.addLinkedWorkflowPhaseTemplate(workflowPhase);
    canaryOrchestrationWorkflow.getWorkflowPhases().add(workflowPhase);
    WorkflowPhase rollbackWorkflowPhase = workflowPhaseHelper.createRollbackPhase(workflowPhase);

    if (createPrimaryPhase) {
      rollbackWorkflowPhase.getPhaseSteps().addAll(k8RollingWorkflowPhaseHelper.getRollbackPhaseSteps());
    } else if (createCanaryPhase) {
      rollbackWorkflowPhase.getPhaseSteps().addAll(k8CanaryWorkflowPhaseHelper.getRollbackPhaseSteps());
    } else {
      workflowPhaseHelper.addK8sEmptyRollbackPhaseStep(rollbackWorkflowPhase);
    }

    workflowServiceTemplateHelper.addLinkedWorkflowPhaseTemplate(rollbackWorkflowPhase);
    canaryOrchestrationWorkflow.getRollbackWorkflowPhaseIdMap().put(workflowPhase.getUuid(), rollbackWorkflowPhase);
  }

  private void updateRollingWorkflowHelper(String accountId, String infraDefinitionId) {
    if (infrastructureDefinitionService
            .getInfraDefById(accountId, infraDefinitionId)
            .getInfrastructure()
            .getInfrastructureType()
            .equals(RANCHER_INFRA_TYPE)) {
      this.k8RollingWorkflowPhaseHelper = this.rancherK8RollingWorkflowPhaseHelper;
    }
  }

  private void updateCanaryWorkflowHelper(String accountId, String infraDefinitionId) {
    if (infrastructureDefinitionService
            .getInfraDefById(accountId, infraDefinitionId)
            .getInfrastructure()
            .getInfrastructureType()
            .equals(RANCHER_INFRA_TYPE)) {
      this.k8CanaryWorkflowPhaseHelper = this.rancherK8CanaryWorkflowPhaseHelper;
    }
  }
}