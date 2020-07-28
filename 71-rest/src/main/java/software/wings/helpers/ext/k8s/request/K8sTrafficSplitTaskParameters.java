package software.wings.helpers.ext.k8s.request;

import io.harness.delegate.task.k8s.K8sTaskType;
import io.harness.k8s.model.HelmVersion;
import lombok.Builder;
import lombok.Data;
import software.wings.beans.k8s.istio.IstioDestinationWeight;

import java.util.List;

@Data
public class K8sTrafficSplitTaskParameters extends K8sTaskParameters {
  private String virtualServiceName;
  private List<IstioDestinationWeight> istioDestinationWeights;

  @Builder
  public K8sTrafficSplitTaskParameters(String accountId, String appId, String commandName, String activityId,
      K8sTaskType k8sTaskType, K8sClusterConfig k8sClusterConfig, String workflowExecutionId, String releaseName,
      Integer timeoutIntervalInMin, String virtualServiceName, List<IstioDestinationWeight> istioDestinationWeights,
      HelmVersion helmVersion) {
    super(accountId, appId, commandName, activityId, k8sClusterConfig, workflowExecutionId, releaseName,
        timeoutIntervalInMin, k8sTaskType, helmVersion);

    this.virtualServiceName = virtualServiceName;
    this.istioDestinationWeights = istioDestinationWeights;
  }
}
