package software.wings.sm.states.rancher;

import static io.harness.annotations.dev.HarnessModule._870_CG_ORCHESTRATION;
import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.BreakDependencyOn;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.context.ContextElementType;

import software.wings.sm.StateType;
import software.wings.sm.states.k8s.K8sRollingDeploy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@TargetModule(_870_CG_ORCHESTRATION)
@OwnedBy(CDP)
@BreakDependencyOn("software.wings.service.intfc.DelegateService")
public class RancherK8sRollingDeploy extends K8sRollingDeploy {
  public RancherK8sRollingDeploy(String name) {
    super(name);
  }

  @Override
  public ContextElementType getRequiredContextElementType() {
    return ContextElementType.RANCHER_K8S_CLUSTER_CRITERIA;
  }
}