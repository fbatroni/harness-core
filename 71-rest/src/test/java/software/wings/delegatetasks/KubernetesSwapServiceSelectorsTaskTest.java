package software.wings.delegatetasks;

import static io.harness.delegate.beans.TaskData.DEFAULT_ASYNC_CALL_TIMEOUT;
import static io.harness.rule.OwnerRule.PUNEET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.harness.beans.ExecutionStatus;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.DelegateTaskPackage;
import io.harness.delegate.beans.TaskData;
import io.harness.k8s.KubernetesContainerService;
import io.harness.rule.Owner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.wings.WingsBaseTest;
import software.wings.beans.TaskType;
import software.wings.beans.container.KubernetesSwapServiceSelectorsParams;
import software.wings.helpers.ext.container.ContainerDeploymentDelegateHelper;
import software.wings.service.impl.ContainerServiceParams;
import software.wings.sm.states.KubernetesSwapServiceSelectorsResponse;

import java.util.Map;

public class KubernetesSwapServiceSelectorsTaskTest extends WingsBaseTest {
  @Mock private DelegateLogService delegateLogService;
  @Mock private ContainerDeploymentDelegateHelper containerDeploymentDelegateHelper;
  @Mock private KubernetesContainerService kubernetesContainerService;

  @InjectMocks
  private KubernetesSwapServiceSelectorsTask kubernetesSwapServiceSelectorsTask =
      (KubernetesSwapServiceSelectorsTask) TaskType.KUBERNETES_SWAP_SERVICE_SELECTORS_TASK.getDelegateRunnableTask(
          DelegateTaskPackage.builder()
              .delegateId("delid1")
              .data(TaskData.builder().async(true).timeout(DEFAULT_ASYNC_CALL_TIMEOUT).build())

              .build(),
          null, notifyResponseData -> {}, () -> true);

  private Service createService(String serviceName, Map<String, String> labelSelectors) {
    ServiceSpecBuilder spec = new ServiceSpecBuilder().withSelector(labelSelectors);

    return new ServiceBuilder().withNewMetadata().withName(serviceName).endMetadata().withSpec(spec.build()).build();
  }

  @Test
  @Owner(developers = PUNEET)
  @Category(UnitTests.class)
  public void smokeTest() {
    Service service1 = createService("service1", ImmutableMap.of("label", "A"));
    Service service2 = createService("service2", ImmutableMap.of("label", "B"));

    when(containerDeploymentDelegateHelper.getKubernetesConfig(any(ContainerServiceParams.class))).thenReturn(null);
    when(kubernetesContainerService.getServiceFabric8(any(), eq(service1.getMetadata().getName())))
        .thenReturn(service1);
    when(kubernetesContainerService.getServiceFabric8(any(), eq(service2.getMetadata().getName())))
        .thenReturn(service2);
    when(kubernetesContainerService.createOrReplaceService(any(), any()))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);

    KubernetesSwapServiceSelectorsParams params = KubernetesSwapServiceSelectorsParams.builder()
                                                      .service1(service1.getMetadata().getName())
                                                      .service2(service2.getMetadata().getName())
                                                      .build();

    KubernetesSwapServiceSelectorsResponse response = kubernetesSwapServiceSelectorsTask.run(new Object[] {params});

    assertThat(response.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);

    ArgumentCaptor<Service> serviceArgumentCaptor = ArgumentCaptor.forClass(Service.class);

    verify(kubernetesContainerService, times(2)).getServiceFabric8(any(), any());

    verify(kubernetesContainerService, times(2)).createOrReplaceService(eq(null), serviceArgumentCaptor.capture());

    Service updatedService1 = serviceArgumentCaptor.getAllValues().get(0);
    assertThat(updatedService1.getMetadata().getName()).isEqualTo("service1");
    assertThat(updatedService1.getSpec().getSelector().get("label")).isEqualTo("B");

    Service updatedService2 = serviceArgumentCaptor.getAllValues().get(1);
    assertThat(updatedService2.getMetadata().getName()).isEqualTo("service2");
    assertThat(updatedService2.getSpec().getSelector().get("label")).isEqualTo("A");
  }
}
