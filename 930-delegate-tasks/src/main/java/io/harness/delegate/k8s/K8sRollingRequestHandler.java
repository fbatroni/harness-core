package io.harness.delegate.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.task.k8s.K8sTaskHelperBase.getTimeoutMillisFromMinutes;
import static io.harness.k8s.K8sCommandUnitConstants.Apply;
import static io.harness.k8s.K8sCommandUnitConstants.FetchFiles;
import static io.harness.k8s.K8sCommandUnitConstants.Init;
import static io.harness.k8s.K8sCommandUnitConstants.Prepare;
import static io.harness.k8s.K8sCommandUnitConstants.WaitForSteadyState;
import static io.harness.k8s.K8sCommandUnitConstants.WrapUp;
import static io.harness.k8s.K8sConstants.MANIFEST_FILES_DIR;
import static io.harness.k8s.manifest.ManifestHelper.getCustomResourceDefinitionWorkloads;
import static io.harness.k8s.manifest.ManifestHelper.getWorkloads;
import static io.harness.k8s.manifest.VersionUtils.addRevisionNumber;
import static io.harness.k8s.manifest.VersionUtils.markVersionedResources;
import static io.harness.logging.CommandExecutionStatus.FAILURE;
import static io.harness.logging.CommandExecutionStatus.SUCCESS;
import static io.harness.logging.LogLevel.ERROR;
import static io.harness.logging.LogLevel.INFO;

import static software.wings.beans.LogColor.Cyan;
import static software.wings.beans.LogColor.White;
import static software.wings.beans.LogColor.Yellow;
import static software.wings.beans.LogHelper.color;
import static software.wings.beans.LogWeight.Bold;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.FileData;
import io.harness.delegate.beans.logstreaming.CommandUnitsProgress;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.task.k8s.ContainerDeploymentDelegateBaseHelper;
import io.harness.delegate.task.k8s.K8sDeployRequest;
import io.harness.delegate.task.k8s.K8sDeployResponse;
import io.harness.delegate.task.k8s.K8sRollingDeployRequest;
import io.harness.delegate.task.k8s.K8sRollingDeployResponse;
import io.harness.delegate.task.k8s.K8sTaskHelperBase;
import io.harness.exception.ExceptionUtils;
import io.harness.exception.InvalidArgumentsException;
import io.harness.k8s.KubernetesContainerService;
import io.harness.k8s.kubectl.Kubectl;
import io.harness.k8s.manifest.ManifestHelper;
import io.harness.k8s.model.K8sDelegateTaskParams;
import io.harness.k8s.model.K8sPod;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.KubernetesResource;
import io.harness.k8s.model.KubernetesResourceId;
import io.harness.k8s.model.Release;
import io.harness.k8s.model.Release.Status;
import io.harness.k8s.model.ReleaseHistory;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.LogCallback;

import com.esotericsoftware.yamlbeans.YamlException;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@OwnedBy(CDP)
@NoArgsConstructor
@Slf4j
public class K8sRollingRequestHandler extends K8sRequestHandler {
  @Inject private transient KubernetesContainerService kubernetesContainerService;
  @Inject private K8sTaskHelperBase k8sTaskHelperBase;
  @Inject K8sRollingBaseHandler k8sRollingBaseHandler;
  @Inject private ContainerDeploymentDelegateBaseHelper containerDeploymentDelegateBaseHelper;

  private KubernetesConfig kubernetesConfig;
  private Kubectl client;
  private ReleaseHistory releaseHistory;
  Release release;
  List<KubernetesResource> customWorkloads;
  List<KubernetesResource> managedWorkloads;
  List<KubernetesResource> resources;
  private String releaseName;
  private String manifestFilesDirectory;

  @Override
  protected K8sDeployResponse executeTaskInternal(K8sDeployRequest k8sDeployRequest,
      K8sDelegateTaskParams k8sDelegateTaskParams, ILogStreamingTaskClient logStreamingTaskClient,
      CommandUnitsProgress commandUnitsProgress) throws Exception {
    if (!(k8sDeployRequest instanceof K8sRollingDeployRequest)) {
      throw new InvalidArgumentsException(Pair.of("k8sDeployRequest", "Must be instance of K8sRollingDeployRequest"));
    }

    K8sRollingDeployRequest k8sRollingDeployRequest = (K8sRollingDeployRequest) k8sDeployRequest;

    releaseName = k8sRollingDeployRequest.getReleaseName();
    manifestFilesDirectory = Paths.get(k8sDelegateTaskParams.getWorkingDirectory(), MANIFEST_FILES_DIR).toString();
    long steadyStateTimeoutInMillis = getTimeoutMillisFromMinutes(k8sDeployRequest.getTimeoutIntervalInMin());

    boolean success = k8sTaskHelperBase.fetchManifestFilesAndWriteToDirectory(
        k8sRollingDeployRequest.getManifestDelegateConfig(), manifestFilesDirectory,
        k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, FetchFiles,
            k8sRollingDeployRequest.isShouldOpenFetchFilesLogStream(), commandUnitsProgress),
        steadyStateTimeoutInMillis, k8sRollingDeployRequest.getAccountId());
    if (!success) {
      return getFailureResponse();
    }

    success = init(k8sRollingDeployRequest, k8sDelegateTaskParams,
        k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, Init, true, commandUnitsProgress));
    if (!success) {
      return getFailureResponse();
    }

    LogCallback prepareLogCallback =
        k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, Prepare, true, commandUnitsProgress);
    success = prepareForRolling(k8sDelegateTaskParams, prepareLogCallback, k8sRollingDeployRequest.isInCanaryWorkflow(),
        k8sRollingDeployRequest.isSkipResourceVersioning());
    if (!success) {
      return getFailureResponse();
    }

    List<KubernetesResource> allWorkloads = ListUtils.union(managedWorkloads, customWorkloads);
    List<K8sPod> existingPodList = k8sRollingBaseHandler.getExistingPods(
        steadyStateTimeoutInMillis, allWorkloads, kubernetesConfig, releaseName, prepareLogCallback);

    success = k8sTaskHelperBase.applyManifests(client, resources, k8sDelegateTaskParams,
        k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, Apply, true, commandUnitsProgress), true);
    if (!success) {
      return getFailureResponse();
    }

    if (isEmpty(managedWorkloads) && isEmpty(customWorkloads)) {
      k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, WaitForSteadyState, true, commandUnitsProgress)
          .saveExecutionLog("Skipping Status Check since there is no Managed Workload.", INFO, SUCCESS);
    } else {
      k8sRollingBaseHandler.setManagedWorkloadsInRelease(k8sDelegateTaskParams, managedWorkloads, release, client);
      k8sRollingBaseHandler.setCustomWorkloadsInRelease(customWorkloads, release);

      kubernetesContainerService.saveReleaseHistory(kubernetesConfig, k8sRollingDeployRequest.getReleaseName(),
          releaseHistory.getAsYaml(), !customWorkloads.isEmpty());

      List<KubernetesResourceId> managedWorkloadKubernetesResourceIds =
          managedWorkloads.stream().map(KubernetesResource::getResourceId).collect(Collectors.toList());
      LogCallback waitForeSteadyStateLogCallback =
          k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, WaitForSteadyState, true, commandUnitsProgress);
      success = k8sTaskHelperBase.doStatusCheckForAllResources(client, managedWorkloadKubernetesResourceIds,
          k8sDelegateTaskParams, kubernetesConfig.getNamespace(), waitForeSteadyStateLogCallback,
          customWorkloads.isEmpty());

      boolean customWorkloadsStatusSuccess = k8sTaskHelperBase.doStatusCheckForAllCustomResources(client,
          customWorkloads, k8sDelegateTaskParams, waitForeSteadyStateLogCallback, true, steadyStateTimeoutInMillis);

      // We have to update the DeploymentConfig revision again as the rollout history command sometimes gives the older
      // revision. There seems to be delay in handling of the DeploymentConfig where it still gives older revision even
      // after the apply command has successfully run
      k8sRollingBaseHandler.updateManagedWorkloadsRevision(k8sDelegateTaskParams, release, client);

      if (!success || !customWorkloadsStatusSuccess) {
        saveRelease(k8sRollingDeployRequest, Status.Failed);
        return getFailureResponse();
      }
    }

    LogCallback executionLogCallback =
        k8sTaskHelperBase.getLogCallback(logStreamingTaskClient, WrapUp, true, commandUnitsProgress);
    k8sRollingBaseHandler.wrapUp(k8sDelegateTaskParams, executionLogCallback, client);

    try {
      String loadBalancer = k8sTaskHelperBase.getLoadBalancerEndpoint(kubernetesConfig, resources);
      K8sRollingDeployResponse rollingSetupResponse =
          K8sRollingDeployResponse.builder()
              .releaseNumber(release.getNumber())
              .k8sPodList(k8sTaskHelperBase.tagNewPods(k8sRollingBaseHandler.getPods(steadyStateTimeoutInMillis,
                                                           allWorkloads, kubernetesConfig, releaseName),
                  existingPodList))
              .loadBalancer(loadBalancer)
              .build();

      saveRelease(k8sRollingDeployRequest, Status.Succeeded);
      executionLogCallback.saveExecutionLog("\nDone.", INFO, CommandExecutionStatus.SUCCESS);

      return K8sDeployResponse.builder()
          .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
          .k8sNGTaskResponse(rollingSetupResponse)
          .build();
    } catch (Exception ex) {
      executionLogCallback.saveExecutionLog(ex.getMessage(), ERROR, FAILURE);
      saveRelease(k8sRollingDeployRequest, Status.Failed);
      throw ex;
    }
  }

  private void saveRelease(K8sRollingDeployRequest k8sRollingDeployRequest, Status status) throws YamlException {
    releaseHistory.setReleaseStatus(status);
    kubernetesContainerService.saveReleaseHistory(kubernetesConfig, k8sRollingDeployRequest.getReleaseName(),
        releaseHistory.getAsYaml(), !customWorkloads.isEmpty());
  }

  private K8sDeployResponse getFailureResponse() {
    K8sRollingDeployResponse rollingSetupResponse = K8sRollingDeployResponse.builder().build();
    return getGenericFailureResponse(rollingSetupResponse);
  }

  @VisibleForTesting
  boolean init(
      K8sRollingDeployRequest request, K8sDelegateTaskParams k8sDelegateTaskParams, LogCallback executionLogCallback) {
    executionLogCallback.saveExecutionLog("Initializing..\n");
    executionLogCallback.saveExecutionLog(color(String.format("Release Name: [%s]", releaseName), Yellow, Bold));
    kubernetesConfig =
        containerDeploymentDelegateBaseHelper.createKubernetesConfig(request.getK8sInfraDelegateConfig());
    client = Kubectl.client(k8sDelegateTaskParams.getKubectlPath(), k8sDelegateTaskParams.getKubeconfigPath());

    try {
      String releaseHistoryData = k8sTaskHelperBase.getReleaseHistoryData(kubernetesConfig, request.getReleaseName());
      releaseHistory = (StringUtils.isEmpty(releaseHistoryData)) ? ReleaseHistory.createNew()
                                                                 : ReleaseHistory.createFromData(releaseHistoryData);

      k8sTaskHelperBase.deleteSkippedManifestFiles(manifestFilesDirectory, executionLogCallback);

      List<String> manifestHelperFiles =
          isEmpty(request.getValuesYamlList()) ? request.getOpenshiftParamList() : request.getValuesYamlList();
      List<FileData> manifestFiles = k8sTaskHelperBase.renderTemplate(k8sDelegateTaskParams,
          request.getManifestDelegateConfig(), manifestFilesDirectory, manifestHelperFiles, releaseName,
          kubernetesConfig.getNamespace(), executionLogCallback, request.getTimeoutIntervalInMin());

      resources = k8sTaskHelperBase.readManifestAndOverrideLocalSecrets(
          manifestFiles, executionLogCallback, request.isLocalOverrideFeatureFlag());
      k8sTaskHelperBase.setNamespaceToKubernetesResourcesIfRequired(resources, kubernetesConfig.getNamespace());

      if (request.isInCanaryWorkflow()) {
        k8sRollingBaseHandler.updateDestinationRuleWithSubsets(executionLogCallback, resources, kubernetesConfig);
        k8sRollingBaseHandler.updateVirtualServiceWithRoutes(executionLogCallback, resources, kubernetesConfig);
      }

      executionLogCallback.saveExecutionLog(color("\nManifests [Post template rendering] :\n", White, Bold));

      executionLogCallback.saveExecutionLog(ManifestHelper.toYamlForLogs(resources));

      if (request.isSkipDryRun()) {
        executionLogCallback.saveExecutionLog(color("\nSkipping Dry Run", Yellow, Bold), INFO);
        executionLogCallback.saveExecutionLog("\nDone.", INFO, CommandExecutionStatus.SUCCESS);
        return true;
      }

      return k8sTaskHelperBase.dryRunManifests(client, resources, k8sDelegateTaskParams, executionLogCallback);
    } catch (Exception e) {
      log.error("Exception:", e);
      executionLogCallback.saveExecutionLog(ExceptionUtils.getMessage(e), ERROR);
      executionLogCallback.saveExecutionLog("\nFailed.", INFO, FAILURE);
      return false;
    }
  }

  private boolean prepareForRolling(K8sDelegateTaskParams k8sDelegateTaskParams, LogCallback executionLogCallback,
      boolean inCanaryWorkflow, boolean skipResourceVersioning) {
    try {
      managedWorkloads = getWorkloads(resources);
      if (isNotEmpty(managedWorkloads) && !skipResourceVersioning) {
        markVersionedResources(resources);
      }

      executionLogCallback.saveExecutionLog("Manifests processed. Found following resources: \n"
          + k8sTaskHelperBase.getResourcesInTableFormat(resources));

      if (!inCanaryWorkflow) {
        release = releaseHistory.createNewRelease(
            resources.stream().map(KubernetesResource::getResourceId).collect(Collectors.toList()));
      } else {
        release = releaseHistory.getLatestRelease();
        release.setResources(resources.stream().map(KubernetesResource::getResourceId).collect(Collectors.toList()));
      }

      executionLogCallback.saveExecutionLog("\nCurrent release number is: " + release.getNumber());

      k8sTaskHelperBase.cleanup(client, k8sDelegateTaskParams, releaseHistory, executionLogCallback);

      customWorkloads = getCustomResourceDefinitionWorkloads(resources);

      if (isEmpty(managedWorkloads) && isEmpty(customWorkloads)) {
        executionLogCallback.saveExecutionLog(color("\nNo Managed Workload found.", Yellow, Bold));
      } else {
        executionLogCallback.saveExecutionLog(color("\nFound following Managed Workloads: \n", Cyan, Bold)
            + k8sTaskHelperBase.getResourcesInTableFormat(ListUtils.union(managedWorkloads, customWorkloads)));

        if (!skipResourceVersioning) {
          executionLogCallback.saveExecutionLog("\nVersioning resources.");
          addRevisionNumber(resources, release.getNumber());
        }

        k8sRollingBaseHandler.addLabelsInManagedWorkloadPodSpec(inCanaryWorkflow, managedWorkloads, releaseName);
        k8sRollingBaseHandler.addLabelsInDeploymentSelectorForCanary(inCanaryWorkflow, managedWorkloads);
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      executionLogCallback.saveExecutionLog(ExceptionUtils.getMessage(e), ERROR, CommandExecutionStatus.FAILURE);
      return false;
    }

    return true;
  }
}
