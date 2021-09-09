package io.harness.cvng;

import static io.harness.cvng.core.utils.DateTimeUtils.roundDownToMinBoundary;
import static io.harness.data.structure.UUIDGenerator.generateUuid;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

import io.harness.cvng.activity.entities.HarnessCDActivity;
import io.harness.cvng.activity.entities.HarnessCDActivity.HarnessCDActivityBuilder;
import io.harness.cvng.beans.CVMonitoringCategory;
import io.harness.cvng.beans.MonitoredServiceDataSourceType;
import io.harness.cvng.beans.MonitoredServiceType;
import io.harness.cvng.beans.job.Sensitivity;
import io.harness.cvng.cdng.beans.CVNGStepInfo;
import io.harness.cvng.cdng.beans.CVNGStepInfo.CVNGStepInfoBuilder;
import io.harness.cvng.cdng.beans.TestVerificationJobSpec;
import io.harness.cvng.cdng.entities.CVNGStepTask;
import io.harness.cvng.cdng.entities.CVNGStepTask.CVNGStepTaskBuilder;
import io.harness.cvng.cdng.entities.CVNGStepTask.Status;
import io.harness.cvng.core.beans.change.event.ChangeEventDTO;
import io.harness.cvng.core.beans.change.event.ChangeEventDTO.ChangeEventDTOBuilder;
import io.harness.cvng.core.beans.change.event.metadata.HarnessCDEventMetaData;
import io.harness.cvng.core.beans.monitoredService.ChangeSourceDTO;
import io.harness.cvng.core.beans.monitoredService.ChangeSourceDTO.ChangeSourceDTOBuilder;
import io.harness.cvng.core.beans.monitoredService.HealthSource;
import io.harness.cvng.core.beans.monitoredService.MetricPackDTO;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO.MonitoredServiceDTOBuilder;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO.MonitoredServiceRef;
import io.harness.cvng.core.beans.monitoredService.changeSourceSpec.HarnessCDChangeSourceSpec;
import io.harness.cvng.core.beans.monitoredService.changeSourceSpec.PagerDutyChangeSourceSpec;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.AppDynamicsHealthSourceSpec;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.HealthSourceSpec;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.beans.params.ServiceEnvironmentParams;
import io.harness.cvng.core.entities.AppDynamicsCVConfig;
import io.harness.cvng.core.entities.AppDynamicsCVConfig.AppDynamicsCVConfigBuilder;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.entities.NewRelicCVConfig;
import io.harness.cvng.core.entities.NewRelicCVConfig.NewRelicCVConfigBuilder;
import io.harness.cvng.core.entities.PrometheusCVConfig;
import io.harness.cvng.core.entities.PrometheusCVConfig.PrometheusCVConfigBuilder;
import io.harness.cvng.core.entities.SplunkCVConfig;
import io.harness.cvng.core.entities.SplunkCVConfig.SplunkCVConfigBuilder;
import io.harness.cvng.core.entities.StackdriverCVConfig;
import io.harness.cvng.core.entities.StackdriverCVConfig.StackdriverCVConfigBuilder;
import io.harness.cvng.core.entities.StackdriverLogCVConfig;
import io.harness.cvng.core.entities.StackdriverLogCVConfig.StackdriverLogCVConfigBuilder;
import io.harness.cvng.core.entities.changeSource.HarnessCDChangeSource;
import io.harness.cvng.core.entities.changeSource.HarnessCDChangeSource.HarnessCDChangeSourceBuilder;
import io.harness.cvng.core.entities.changeSource.PagerDutyChangeSource;
import io.harness.cvng.core.entities.changeSource.PagerDutyChangeSource.PagerDutyChangeSourceBuilder;
import io.harness.cvng.core.types.ChangeSourceType;
import io.harness.cvng.dashboard.entities.HeatMap;
import io.harness.cvng.dashboard.entities.HeatMap.HeatMapBuilder;
import io.harness.cvng.dashboard.entities.HeatMap.HeatMapResolution;
import io.harness.cvng.dashboard.entities.HeatMap.HeatMapRisk;
import io.harness.cvng.verificationjob.entities.TestVerificationJob;
import io.harness.cvng.verificationjob.entities.VerificationJob;
import io.harness.cvng.verificationjob.entities.VerificationJobInstance;
import io.harness.cvng.verificationjob.entities.VerificationJobInstance.VerificationJobInstanceBuilder;
import io.harness.eventsframework.schemas.deployment.ArtifactDetails;
import io.harness.eventsframework.schemas.deployment.DeploymentEventDTO;
import io.harness.eventsframework.schemas.deployment.ExecutionDetails;
import io.harness.ng.core.environment.dto.EnvironmentResponseDTO;
import io.harness.ng.core.environment.dto.EnvironmentResponseDTO.EnvironmentResponseDTOBuilder;
import io.harness.ng.core.service.dto.ServiceResponseDTO;
import io.harness.ng.core.service.dto.ServiceResponseDTO.ServiceResponseDTOBuilder;
import io.harness.pms.yaml.ParameterField;

import com.google.common.collect.Sets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Data
@Builder(buildMethodName = "unsafeBuild")
public class BuilderFactory {
  public static final String CONNECTOR_IDENTIFIER = "connectorIdentifier";
  @Getter @Setter(AccessLevel.PRIVATE) private Clock clock;
  @Getter @Setter(AccessLevel.PRIVATE) private Context context;
  public static class BuilderFactoryBuilder {
    public BuilderFactory build() {
      BuilderFactory builder = unsafeBuild();
      if (builder.clock == null) {
        builder.setClock(Clock.fixed(Instant.parse("2020-04-22T10:02:06Z"), ZoneOffset.UTC));
      }
      if (builder.getContext() == null) {
        builder.setContext(Context.defaultContext());
      }
      return builder;
    }
  }

  public CVNGStepTaskBuilder cvngStepTaskBuilder() {
    return CVNGStepTask.builder()
        .accountId(context.getAccountId())
        .activityId(generateUuid())
        .status(Status.IN_PROGRESS)
        .callbackId(generateUuid());
  }

  public ServiceResponseDTOBuilder serviceResponseDTOBuilder() {
    return ServiceResponseDTO.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .identifier(context.getServiceIdentifier())
        .projectIdentifier(context.getProjectIdentifier());
  }

  public EnvironmentResponseDTOBuilder environmentResponseDTOBuilder() {
    return EnvironmentResponseDTO.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .identifier(context.getEnvIdentifier())
        .projectIdentifier(context.getProjectIdentifier());
  }

  public VerificationJobInstanceBuilder verificationJobInstanceBuilder() {
    CVConfig cvConfig = appDynamicsCVConfigBuilder().uuid(generateUuid()).build();
    Map<String, CVConfig> cvConfigMap = Collections.singletonMap(cvConfig.getUuid(), cvConfig);
    return VerificationJobInstance.builder()
        .accountId(context.getAccountId())
        .deploymentStartTime(clock.instant().minus(Duration.ofMinutes(2)))
        .startTime(clock.instant())
        .cvConfigMap(cvConfigMap)
        .dataCollectionDelay(Duration.ofMinutes(2))
        .resolvedJob(getVerificationJob());
  }

  public MonitoredServiceDTOBuilder monitoredServiceDTOBuilder() {
    return MonitoredServiceDTO.builder()
        .identifier(context.serviceIdentifier + "_" + context.getEnvIdentifier())
        .name("monitored service name")
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .type(MonitoredServiceType.APPLICATION)
        .description(generateUuid())
        .serviceRef(context.getServiceIdentifier())
        .environmentRef(context.getEnvIdentifier())
        .dependencies(Sets.newHashSet(MonitoredServiceRef.builder().monitoredServiceIdentifier("service1").build(),
            MonitoredServiceRef.builder().monitoredServiceIdentifier("service2").build()))
        .sources(MonitoredServiceDTO.Sources.builder()
                     .healthSources(Arrays.asList(createHealthSource()).stream().collect(Collectors.toSet()))
                     .build());
  }

  public HeatMapBuilder heatMapBuilder() {
    Instant bucketEndTime = clock.instant();
    bucketEndTime = roundDownToMinBoundary(bucketEndTime, 30);
    Instant bucketStartTime = bucketEndTime.minus(24, ChronoUnit.HOURS);
    List<HeatMapRisk> heatMapRisks = new ArrayList<>();

    int index = 0;
    for (Instant startTime = bucketStartTime; startTime.isBefore(bucketEndTime);
         startTime = startTime.plus(30, ChronoUnit.MINUTES)) {
      heatMapRisks.add(HeatMapRisk.builder()
                           .riskScore(-1)
                           .startTime(startTime)
                           .endTime(startTime.plus(30, ChronoUnit.MINUTES))
                           .anomalousLogsCount(index)
                           .anomalousMetricsCount(index + 1)
                           .build());
      index++;
    }

    return HeatMap.builder()
        .accountId(context.getAccountId())
        .projectIdentifier(context.getProjectIdentifier())
        .orgIdentifier(context.getOrgIdentifier())
        .category(CVMonitoringCategory.ERRORS)
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .heatMapResolution(HeatMapResolution.THIRTY_MINUTES)
        .heatMapBucketStartTime(bucketStartTime)
        .heatMapBucketEndTime(bucketEndTime)
        .heatMapRisks(heatMapRisks);
  }

  private HealthSource createHealthSource() {
    return HealthSource.builder()
        .identifier("healthSourceIdentifier")
        .name("health source name")
        .type(MonitoredServiceDataSourceType.APP_DYNAMICS)
        .spec(createHealthSourceSpec())
        .build();
  }

  private HealthSourceSpec createHealthSourceSpec() {
    return AppDynamicsHealthSourceSpec.builder()
        .applicationName("appApplicationName")
        .tierName("tier")
        .connectorRef(CONNECTOR_IDENTIFIER)
        .feature("Application Monitoring")
        .metricPacks(new HashSet<MetricPackDTO>() {
          { add(MetricPackDTO.builder().identifier(CVMonitoringCategory.ERRORS).build()); }
        })
        .build();
  }

  public CVNGStepInfoBuilder cvngStepInfoBuilder() {
    return CVNGStepInfo.builder()
        .type("LoadTest")
        .spec(TestVerificationJobSpec.builder()
                  .duration(ParameterField.createValueField("5m"))
                  .deploymentTag(ParameterField.createValueField("build#1"))
                  .sensitivity(ParameterField.createValueField("Low"))
                  .build());
  }

  public AppDynamicsCVConfigBuilder appDynamicsCVConfigBuilder() {
    return AppDynamicsCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .identifier(generateUuid())
        .monitoringSourceName(generateUuid())
        .metricPack(MetricPack.builder().build())
        .applicationName(generateUuid())
        .tierName(generateUuid())
        .connectorIdentifier("AppDynamics Connector")
        .category(CVMonitoringCategory.PERFORMANCE)
        .productName(generateUuid());
  }

  public StackdriverLogCVConfigBuilder stackdriverLogCVConfigBuilder() {
    return StackdriverLogCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .queryName(randomAlphabetic(10))
        .query(randomAlphabetic(10))
        .messageIdentifier(randomAlphabetic(10))
        .serviceInstanceIdentifier(randomAlphabetic(10))
        .identifier(generateUuid())
        .monitoringSourceName(generateUuid())
        .connectorIdentifier("StackdriverLog Connector")
        .category(CVMonitoringCategory.ERRORS)
        .productName(generateUuid());
  }

  public NewRelicCVConfigBuilder newRelicCVConfigBuilder() {
    return NewRelicCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier());
  }

  public PrometheusCVConfigBuilder prometheusCVConfigBuilder() {
    return PrometheusCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .connectorIdentifier("connectorRef")
        .category(CVMonitoringCategory.PERFORMANCE);
  }

  public StackdriverCVConfigBuilder stackdriverMetricCVConfigBuilder() {
    return StackdriverCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .connectorIdentifier("connectorRef")
        .dashboardName("dashboardName")
        .category(CVMonitoringCategory.PERFORMANCE);
  }

  public SplunkCVConfigBuilder splunkCVConfigBuilder() {
    return SplunkCVConfig.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .serviceInstanceIdentifier(randomAlphabetic(10))
        .envIdentifier(context.getEnvIdentifier())
        .queryName(randomAlphabetic(10))
        .query(randomAlphabetic(10))
        .serviceInstanceIdentifier(randomAlphabetic(10))
        .identifier(generateUuid())
        .monitoringSourceName(generateUuid())
        .connectorIdentifier("Splunk Connector")
        .category(CVMonitoringCategory.ERRORS)
        .productName(generateUuid());
  }

  public HarnessCDChangeSourceBuilder getHarnessCDChangeSourceBuilder() {
    return HarnessCDChangeSource.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .enabled(true)
        .type(ChangeSourceType.HARNESS_CD);
  }

  public PagerDutyChangeSourceBuilder getPagerDutyChangeSourceBuilder() {
    return PagerDutyChangeSource.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .enabled(true)
        .connectorIdentifier(randomAlphabetic(20))
        .pagerDutyServiceId(randomAlphabetic(20))
        .type(ChangeSourceType.PAGER_DUTY);
  }

  public ChangeSourceDTOBuilder getHarnessCDChangeSourceDTOBuilder() {
    return getChangeSourceDTOBuilder(ChangeSourceType.HARNESS_CD).spec(new HarnessCDChangeSourceSpec());
  }

  public ChangeSourceDTOBuilder getPagerDutyChangeSourceDTOBuilder() {
    return getChangeSourceDTOBuilder(ChangeSourceType.PAGER_DUTY)
        .spec(PagerDutyChangeSourceSpec.builder()
                  .connectorRef(randomAlphabetic(20))
                  .pagerDutyServiceId(randomAlphabetic(20))
                  .build());
  }

  public HarnessCDActivityBuilder getHarnessCDActivityBuilder() {
    return HarnessCDActivity.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .environmentIdentifier(context.getEnvIdentifier())
        .eventTime(clock.instant())
        .changeSourceIdentifier("changeSourceID")
        .type(ChangeSourceType.HARNESS_CD.getActivityType())
        .stageStepId("stageStepId")
        .stageId("stageId")
        .pipelineId("pipelineId")
        .planExecutionId("executionId")
        .artifactType("artifactType")
        .artifactTag("artifactTag")
        .deploymentStatus("status")
        .activityEndTime(clock.instant())
        .activityStartTime(clock.instant());
  }

  public ChangeEventDTOBuilder getHarnessCDChangeEventDTOBuilder() {
    return getChangeEventDTOBuilder()
        .type(ChangeSourceType.HARNESS_CD)
        .changeEventMetaData(HarnessCDEventMetaData.builder()
                                 .stageStepId("stage")
                                 .planExecutionId("executionId")
                                 .deploymentEndTime(Instant.now().toEpochMilli())
                                 .deploymentStartTime(Instant.now().toEpochMilli())
                                 .stageStepId("stageStepId")
                                 .stageId("stageId")
                                 .pipelineId("pipelineId")
                                 .planExecutionId("executionId")
                                 .artifactType("artifactType")
                                 .artifactTag("artifactTag")
                                 .status("status")
                                 .build());
  }

  public ChangeEventDTOBuilder getChangeEventDTOBuilder() {
    return ChangeEventDTO.builder()
        .accountId(context.getAccountId())
        .orgIdentifier(context.getOrgIdentifier())
        .projectIdentifier(context.getProjectIdentifier())
        .serviceIdentifier(context.getServiceIdentifier())
        .envIdentifier(context.getEnvIdentifier())
        .eventTime(Instant.EPOCH.getEpochSecond())
        .changeSourceIdentifier("changeSourceID");
  }

  public DeploymentEventDTO.Builder getDeploymentEventDTOBuilder() {
    return DeploymentEventDTO.newBuilder()
        .setAccountId(context.getAccountId())
        .setOrgIdentifier(context.getOrgIdentifier())
        .setProjectIdentifier(context.getProjectIdentifier())
        .setServiceIdentifier(context.getServiceIdentifier())
        .setEnvironmentIdentifier(context.getEnvIdentifier())
        .setDeploymentStartTime(Instant.now().toEpochMilli())
        .setDeploymentEndTime(Instant.now().toEpochMilli())
        .setDeploymentStatus("SUCCESS")
        .setExecutionDetails(ExecutionDetails.newBuilder()
                                 .setStageId("stageId")
                                 .setPipelineId("pipelineId")
                                 .setPlanExecutionId("planExecutionId")
                                 .setStageSetupId("stageStepId")
                                 .build())
        .setArtifactDetails(
            ArtifactDetails.newBuilder().setArtifactTag("artifactTag").setArtifactType("artifactType").build());
  }

  private ChangeSourceDTOBuilder getChangeSourceDTOBuilder(ChangeSourceType changeSourceType) {
    return ChangeSourceDTO.builder()
        .identifier(generateUuid())
        .name(generateUuid())
        .enabled(true)
        .type(changeSourceType);
  }

  private VerificationJob getVerificationJob() {
    TestVerificationJob testVerificationJob = new TestVerificationJob();
    testVerificationJob.setAccountId(context.getAccountId());
    testVerificationJob.setIdentifier("identifier");
    testVerificationJob.setJobName(generateUuid());
    testVerificationJob.setMonitoringSources(Arrays.asList("monitoringIdentifier"));
    testVerificationJob.setSensitivity(Sensitivity.MEDIUM);
    testVerificationJob.setServiceIdentifier(context.getServiceIdentifier(), false);
    testVerificationJob.setEnvIdentifier(context.getEnvIdentifier(), false);
    testVerificationJob.setBaselineVerificationJobInstanceId(generateUuid());
    testVerificationJob.setDuration(Duration.ofMinutes(5));
    testVerificationJob.setProjectIdentifier(context.getProjectIdentifier());
    testVerificationJob.setOrgIdentifier(context.getOrgIdentifier());
    return testVerificationJob;
  }

  public static BuilderFactory getDefault() {
    return BuilderFactory.builder().build();
  }

  @Value
  @Builder
  public static class Context {
    ProjectParams projectParams;
    String serviceIdentifier;
    String envIdentifier;

    public static Context defaultContext() {
      return Context.builder()
          .projectParams(ProjectParams.builder()
                             .accountIdentifier(randomAlphabetic(20))
                             .orgIdentifier(randomAlphabetic(20))
                             .projectIdentifier(randomAlphabetic(20))
                             .build())
          .envIdentifier(randomAlphabetic(20))
          .serviceIdentifier(randomAlphabetic(20))
          .build();
    }

    public String getAccountId() {
      return projectParams.getAccountIdentifier();
    }

    public String getOrgIdentifier() {
      return projectParams.getOrgIdentifier();
    }

    public String getProjectIdentifier() {
      return projectParams.getProjectIdentifier();
    }

    public ServiceEnvironmentParams getServiceEnvironmentParams() {
      return ServiceEnvironmentParams.builder()
          .accountIdentifier(projectParams.getAccountIdentifier())
          .orgIdentifier(projectParams.getOrgIdentifier())
          .projectIdentifier(projectParams.getProjectIdentifier())
          .serviceIdentifier(serviceIdentifier)
          .environmentIdentifier(envIdentifier)
          .build();
    }
  }
}
