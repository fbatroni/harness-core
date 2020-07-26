package io.harness.perpetualtask.instancesync;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.service.InstanceSyncConstants.HARNESS_APPLICATION_ID;
import static software.wings.service.InstanceSyncConstants.INFRASTRUCTURE_MAPPING_ID;
import static software.wings.service.InstanceSyncConstants.TIMEOUT_SECONDS;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import io.harness.beans.DelegateTask;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.task.spotinst.request.SpotInstListElastigroupInstancesParameters;
import io.harness.perpetualtask.PerpetualTaskClientContext;
import io.harness.perpetualtask.PerpetualTaskResponse;
import io.harness.perpetualtask.PerpetualTaskServiceClient;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.serializer.KryoSerializer;
import io.harness.tasks.Cd1SetupFields;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import software.wings.beans.AwsAmiInfrastructureMapping;
import software.wings.beans.AwsConfig;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SpotInstConfig;
import software.wings.beans.TaskType;
import software.wings.service.impl.spotinst.SpotInstCommandRequest;
import software.wings.service.intfc.InfrastructureMappingService;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.security.SecretManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpotinstAmiInstanceSyncPerpetualTaskClient implements PerpetualTaskServiceClient {
  public static final String ELASTIGROUP_ID = "elastigroupId";
  @Inject InfrastructureMappingService infraMappingService;
  @Inject SettingsService settingsService;
  @Inject SecretManager secretManager;
  @Inject private KryoSerializer kryoSerializer;

  @Override
  public Message getTaskParams(PerpetualTaskClientContext clientContext) {
    final PerpetualTaskData delegateTaskData = getPerpetualTaskData(clientContext);

    ByteString awsConfigBytes = ByteString.copyFrom(kryoSerializer.asBytes(delegateTaskData.getAwsConfig()));
    ByteString spotinstConfigBytes = ByteString.copyFrom(kryoSerializer.asBytes(delegateTaskData.getSpotinstConfig()));
    ByteString awsEncryptedDataBytes =
        ByteString.copyFrom(kryoSerializer.asBytes(delegateTaskData.getAwsEncryptedDataData()));
    ByteString spotinstEncryptedDataBytes =
        ByteString.copyFrom(kryoSerializer.asBytes(delegateTaskData.getSpotinstEncryptedData()));

    return SpotinstAmiInstanceSyncPerpetualTaskParams.newBuilder()
        .setRegion(delegateTaskData.getRegion())
        .setElastigroupId(delegateTaskData.getElastigroupId())
        .setAwsConfig(awsConfigBytes)
        .setAwsEncryptedData(awsEncryptedDataBytes)
        .setSpotinstConfig(spotinstConfigBytes)
        .setSpotinstEncryptedData(spotinstEncryptedDataBytes)
        .build();
  }

  @Override
  public void onTaskStateChange(
      String taskId, PerpetualTaskResponse newPerpetualTaskResponse, PerpetualTaskResponse oldPerpetualTaskResponse) {
    // Instance Sync Perpetual Task Framework takes care of this via Perpetual Task Response
  }

  @Override
  public DelegateTask getValidationTask(PerpetualTaskClientContext clientContext, String accountId) {
    final PerpetualTaskData perpetualTaskData = getPerpetualTaskData(clientContext);
    final SpotInstListElastigroupInstancesParameters spotinstParams =
        SpotInstListElastigroupInstancesParameters.builder()
            .awsRegion(perpetualTaskData.getRegion())
            .elastigroupId(perpetualTaskData.getElastigroupId())
            .build();

    return DelegateTask.builder()
        .accountId(accountId)
        .setupAbstraction(Cd1SetupFields.APP_ID_FIELD, GLOBAL_APP_ID)
        .tags(isNotEmpty(perpetualTaskData.awsConfig.getTag())
                ? Collections.singletonList(perpetualTaskData.awsConfig.getTag())
                : null)
        .data(TaskData.builder()
                  .async(false)
                  .taskType(TaskType.SPOTINST_COMMAND_TASK.name())
                  .parameters(new Object[] {SpotInstCommandRequest.builder()
                                                .awsConfig(perpetualTaskData.awsConfig)
                                                .awsEncryptionDetails(perpetualTaskData.awsEncryptedDataData)
                                                .spotInstConfig(perpetualTaskData.spotinstConfig)
                                                .spotinstEncryptionDetails(perpetualTaskData.spotinstEncryptedData)
                                                .spotInstTaskParameters(spotinstParams)
                                                .build()})
                  .timeout(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
                  .build())
        .build();
  }

  private PerpetualTaskData getPerpetualTaskData(PerpetualTaskClientContext clientContext) {
    Map<String, String> clientParams = clientContext.getClientParams();
    String appId = clientParams.get(HARNESS_APPLICATION_ID);
    String infraMappingId = clientParams.get(INFRASTRUCTURE_MAPPING_ID);
    String elastigroupId = clientParams.get(ELASTIGROUP_ID);
    AwsAmiInfrastructureMapping infraMapping =
        (AwsAmiInfrastructureMapping) infraMappingService.get(appId, infraMappingId);

    SettingAttribute spotinstSettingAttribute = settingsService.get(infraMapping.getSpotinstCloudProvider());
    SpotInstConfig spotinstConfig = (SpotInstConfig) spotinstSettingAttribute.getValue();
    List<EncryptedDataDetail> spotinstEncryptedDetails = secretManager.getEncryptionDetails(spotinstConfig, null, null);

    SettingAttribute awsSettingAttribute = settingsService.get(infraMapping.getComputeProviderSettingId());
    AwsConfig awsConfig = (AwsConfig) awsSettingAttribute.getValue();
    List<EncryptedDataDetail> awsEncryptedDataDetails = secretManager.getEncryptionDetails(awsConfig, null, null);

    return PerpetualTaskData.builder()
        .region(infraMapping.getRegion())
        .elastigroupId(elastigroupId)
        .spotinstConfig(spotinstConfig)
        .awsConfig(awsConfig)
        .spotinstEncryptedData(spotinstEncryptedDetails)
        .awsEncryptedDataData(awsEncryptedDataDetails)
        .build();
  }

  @Data
  @Builder
  static class PerpetualTaskData {
    String region;
    String elastigroupId;
    SpotInstConfig spotinstConfig;
    AwsConfig awsConfig;
    List<EncryptedDataDetail> spotinstEncryptedData;
    List<EncryptedDataDetail> awsEncryptedDataData;
  }
}
