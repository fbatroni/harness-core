package io.harness.cvng.activity.services.impl;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.PRAVEEN;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CvNextGenTestBase;
import io.harness.category.element.UnitTests;
import io.harness.cvng.BuilderFactory;
import io.harness.cvng.activity.entities.KubernetesClusterActivity;
import io.harness.cvng.activity.entities.KubernetesClusterActivity.ServiceEnvironment;
import io.harness.cvng.core.beans.dependency.KubernetesDependencyMetadata;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO.ServiceDependencyDTO;
import io.harness.cvng.core.services.api.monitoredService.MonitoredServiceService;
import io.harness.cvng.core.services.api.monitoredService.ServiceDependencyService;
import io.harness.rule.Owner;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class KubernetesClusterActivityUpdateHandlerTest extends CvNextGenTestBase {
  @Inject KubernetesClusterActivityUpdateHandler kubernetesClusterActivityUpdateHandler;
  @Inject MonitoredServiceService monitoredServiceService;
  @Inject ServiceDependencyService serviceDependencyService;

  BuilderFactory builderFactory;
  String serviceIdentifier;
  String envIdentifier;

  @Before
  public void setup() {
    builderFactory = BuilderFactory.getDefault();
    serviceIdentifier = generateUuid();
    envIdentifier = generateUuid();
  }

  @Test
  @Owner(developers = PRAVEEN)
  @Category(UnitTests.class)
  public void testCreate() {
    KubernetesClusterActivity clusterActivity = builderFactory.getKubernetesClusterActivityBuilder().build();
    assertThat(clusterActivity.getRelatedAppServices()).isNullOrEmpty();

    String infraServiceIdentifier = generateUuid();
    MonitoredServiceDTO infraService = builderFactory.monitoredServiceDTOBuilder()
                                           .identifier(infraServiceIdentifier)
                                           .serviceRef(clusterActivity.getServiceIdentifier())
                                           .environmentRef(clusterActivity.getEnvironmentIdentifier())
                                           .build();
    infraService.getSources().setHealthSources(null);
    infraService.getSources().setChangeSources(
        Sets.newHashSet(builderFactory.getKubernetesChangeSourceDTOBuilder().build()));

    monitoredServiceService.create(clusterActivity.getAccountId(), infraService);

    MonitoredServiceDTO appService =
        builderFactory.monitoredServiceDTOBuilder()
            .identifier(generateUuid())
            .serviceRef(serviceIdentifier)
            .environmentRef(envIdentifier)
            .dependencies(Sets.newHashSet(ServiceDependencyDTO.builder()
                                              .monitoredServiceIdentifier(infraServiceIdentifier)
                                              .dependencyMetadata(KubernetesDependencyMetadata.builder()
                                                                      .namespace(clusterActivity.getNamespace())
                                                                      .workload(clusterActivity.getWorkload())
                                                                      .build())
                                              .build()))
            .build();
    appService.getSources().setHealthSources(null);
    appService.getSources().setChangeSources(
        Sets.newHashSet(builderFactory.getHarnessCDChangeSourceDTOBuilder().build()));
    monitoredServiceService.create(clusterActivity.getAccountId(), appService);

    kubernetesClusterActivityUpdateHandler.handleCreate(clusterActivity);
    assertThat(clusterActivity.getRelatedAppServices()).isNotEmpty();
    assertThat(clusterActivity.getRelatedAppServices().size()).isEqualTo(1);
    ServiceEnvironment serviceEnvironment = clusterActivity.getRelatedAppServices().get(0);

    assertThat(serviceEnvironment.getServiceIdentifier()).isEqualTo(appService.getServiceRef());
    assertThat(serviceEnvironment.getEnvironmentIdentifier()).isEqualTo(appService.getEnvironmentRef());
  }

  @Test
  @Owner(developers = PRAVEEN)
  @Category(UnitTests.class)
  public void testCreate_noDependency() {
    KubernetesClusterActivity clusterActivity = builderFactory.getKubernetesClusterActivityBuilder().build();
    assertThat(clusterActivity.getRelatedAppServices()).isNullOrEmpty();

    String infraServiceIdentifier = generateUuid();
    MonitoredServiceDTO infraService = builderFactory.monitoredServiceDTOBuilder()
                                           .identifier(infraServiceIdentifier)
                                           .serviceRef(clusterActivity.getServiceIdentifier())
                                           .environmentRef(clusterActivity.getEnvironmentIdentifier())
                                           .build();
    infraService.getSources().setHealthSources(null);
    infraService.getSources().setChangeSources(
        Sets.newHashSet(builderFactory.getKubernetesChangeSourceDTOBuilder().build()));

    monitoredServiceService.create(clusterActivity.getAccountId(), infraService);

    MonitoredServiceDTO appService = builderFactory.monitoredServiceDTOBuilder()
                                         .identifier(generateUuid())
                                         .serviceRef(serviceIdentifier)
                                         .environmentRef(envIdentifier)
                                         .build();
    appService.getSources().setHealthSources(null);
    appService.getSources().setChangeSources(
        Sets.newHashSet(builderFactory.getHarnessCDChangeSourceDTOBuilder().build()));
    monitoredServiceService.create(clusterActivity.getAccountId(), appService);

    kubernetesClusterActivityUpdateHandler.handleCreate(clusterActivity);
    assertThat(clusterActivity.getRelatedAppServices()).isNullOrEmpty();
  }
}