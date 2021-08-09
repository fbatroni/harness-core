package io.harness.pms.expressions.utils;

import static io.harness.rule.OwnerRule.BRIJESH;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.PipelineServiceTestBase;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.DelegateResponseData;
import io.harness.delegate.beans.connector.awsconnector.AwsConnectorDTO;
import io.harness.delegate.beans.connector.awsconnector.AwsCredentialDTO;
import io.harness.delegate.beans.connector.awsconnector.AwsManualConfigSpecDTO;
import io.harness.delegate.task.artifacts.ArtifactTaskType;
import io.harness.delegate.task.artifacts.ecr.EcrArtifactDelegateRequest;
import io.harness.delegate.task.artifacts.response.ArtifactTaskExecutionResponse;
import io.harness.delegate.task.artifacts.response.ArtifactTaskResponse;
import io.harness.engine.pms.tasks.NgDelegate2TaskExecutor;
import io.harness.logging.CommandExecutionStatus;
import io.harness.ng.core.BaseNGAccess;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.rule.Owner;
import io.harness.secretmanagerclient.services.api.SecretManagerClientService;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.serializer.KryoSerializer;
import io.harness.service.DelegateGrpcClientWrapper;
import io.harness.tasks.BinaryResponseData;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.PIPELINE)
public class EcrImagePullSecretHelperTest extends PipelineServiceTestBase {
  @Mock private SecretManagerClientService secretManagerClientService;
  @Mock private DelegateGrpcClientWrapper delegateGrpcClientWrapper;
  @Mock private NgDelegate2TaskExecutor ngDelegate2TaskExecutor;
  @Mock private KryoSerializer kryoSerializer;
  public static String accountId = "accountId";
  public static String orgId = "orgId";
  public static String projectId = "projectId";
  @InjectMocks private EcrImagePullSecretHelper ecrImagePullSecretHelper;
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testExecuteSyncTask() {
    ArtifactTaskExecutionResponse artifactTaskExecutionResponse = ArtifactTaskExecutionResponse.builder().build();
    DelegateResponseData response = ArtifactTaskResponse.builder()
                                        .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                                        .artifactTaskExecutionResponse(artifactTaskExecutionResponse)
                                        .build();
    BinaryResponseData binaryResponseData = BinaryResponseData.builder().build();
    Ambiance ambiance = Ambiance.newBuilder().putSetupAbstractions("accountId", accountId).build();
    BaseNGAccess baseNGAccess =
        BaseNGAccess.builder().accountIdentifier(accountId).orgIdentifier(orgId).projectIdentifier(projectId).build();
    EcrArtifactDelegateRequest ecrArtifactDelegateRequest =
        EcrArtifactDelegateRequest.builder().imagePath("imagePath").tag("1.0").build();
    when(kryoSerializer.asDeflatedBytes(any())).thenReturn("".getBytes());
    when(kryoSerializer.asInflatedObject(any())).thenReturn(response);
    when(ngDelegate2TaskExecutor.executeTask(any(), any())).thenReturn(binaryResponseData);
    assertThat(ecrImagePullSecretHelper
                   .executeSyncTask(ambiance, ecrArtifactDelegateRequest, ArtifactTaskType.GET_IMAGE_URL, baseNGAccess,
                       "execute sync task failed")
                   .equals(artifactTaskExecutionResponse));
    verify(ngDelegate2TaskExecutor, atLeastOnce()).executeTask(any(), any());
    verify(kryoSerializer, atLeastOnce()).asDeflatedBytes(any());
    verify(kryoSerializer, atLeastOnce()).asInflatedObject(any());
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testGetBaseNGAccess() {
    BaseNGAccess baseNGAccess = ecrImagePullSecretHelper.getBaseNGAccess(accountId, orgId, projectId);
    assertEquals(baseNGAccess.getAccountIdentifier(), accountId);
    assertEquals(baseNGAccess.getOrgIdentifier(), orgId);
    assertEquals(baseNGAccess.getProjectIdentifier(), projectId);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testGetEncryptionDetails() {
    BaseNGAccess baseNGAccess =
        BaseNGAccess.builder().accountIdentifier(accountId).orgIdentifier(orgId).projectIdentifier(projectId).build();

    List<EncryptedDataDetail> encryptedDataDetailList =
        Collections.singletonList(EncryptedDataDetail.builder().build());
    when(secretManagerClientService.getEncryptionDetails(eq(baseNGAccess), any())).thenReturn(encryptedDataDetailList);
    AwsConnectorDTO awsConnectorDTO = AwsConnectorDTO.builder().build();
    assertEquals(ecrImagePullSecretHelper.getEncryptionDetails(awsConnectorDTO, baseNGAccess).size(), 0);
    awsConnectorDTO =
        AwsConnectorDTO.builder()
            .credential(AwsCredentialDTO.builder().config(AwsManualConfigSpecDTO.builder().build()).build())
            .build();
    assertEquals(ecrImagePullSecretHelper.getEncryptionDetails(awsConnectorDTO, baseNGAccess).size(), 1);
  }
}