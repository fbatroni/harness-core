package io.harness.delegate.task.git;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.ABHINAV2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.DelegateTaskPackage;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.connector.scm.ScmConnector;
import io.harness.delegate.beans.connector.scm.genericgitconnector.GitConfigDTO;
import io.harness.delegate.beans.git.GitCommandExecutionResponse;
import io.harness.delegate.beans.git.GitCommandExecutionResponse.GitCommandStatus;
import io.harness.delegate.beans.git.GitCommandParams;
import io.harness.delegate.beans.git.GitCommandType;
import io.harness.delegate.git.NGGitService;
import io.harness.delegate.task.TaskParameters;
import io.harness.git.model.CommitAndPushRequest;
import io.harness.git.model.CommitAndPushResult;
import io.harness.rule.Owner;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.shell.SshSessionConfig;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.CDP)
public class NGGitCommandTaskTest extends CategoryTest {
  private static final String TEST_INPUT_ID = generateUuid();

  @Mock private NGGitService gitService;
  @Mock private GitCommandTaskHandler gitCommandTaskHandler;
  @Mock private GitDecryptionHelper gitDecryptionHelper;
  @InjectMocks
  NGGitCommandTask ngGitCommandTask = new NGGitCommandTask(
      DelegateTaskPackage.builder()
          .delegateId(TEST_INPUT_ID)
          .delegateTaskId(TEST_INPUT_ID)
          .data(TaskData.builder().parameters(new Object[] {}).taskType(TEST_INPUT_ID).async(false).build())
          .accountId(TEST_INPUT_ID)
          .build(),
      null, null, null);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doNothing()
        .when(gitDecryptionHelper)
        .decryptGitConfig(any(GitConfigDTO.class), anyListOf(EncryptedDataDetail.class));
    doNothing()
        .when(gitDecryptionHelper)
        .decryptApiAccessConfig(any(ScmConnector.class), anyListOf(EncryptedDataDetail.class));
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testTaskRunWithInvalidParams() {
    assertThatThrownBy(() -> ngGitCommandTask.run(new Object[] {})).isInstanceOf(NotImplementedException.class);
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testGitValidate() {
    GitCommandExecutionResponse gitCommandExecutionResponse =
        GitCommandExecutionResponse.builder().gitCommandStatus(GitCommandStatus.SUCCESS).build();
    doReturn(gitCommandExecutionResponse)
        .when(gitCommandTaskHandler)
        .handleValidateTask(
            any(GitConfigDTO.class), any(ScmConnector.class), any(String.class), any(SshSessionConfig.class));
    TaskParameters params = GitCommandParams.builder()
                                .gitConfig(GitConfigDTO.builder().build())
                                .gitCommandType(GitCommandType.VALIDATE)
                                .build();

    GitCommandExecutionResponse response = (GitCommandExecutionResponse) ngGitCommandTask.run(params);
    assertThat(response.getGitCommandStatus()).isEqualTo(GitCommandStatus.SUCCESS);
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testGitCommitAndPush() {
    CommitAndPushResult gitCommitAndPushResult = CommitAndPushResult.builder().build();
    doReturn(gitCommitAndPushResult)
        .when(gitService)
        .commitAndPush(
            any(GitConfigDTO.class), any(CommitAndPushRequest.class), anyString(), any(SshSessionConfig.class));
    TaskParameters params = GitCommandParams.builder()
                                .gitConfig(GitConfigDTO.builder().build())
                                .gitCommandType(GitCommandType.COMMIT_AND_PUSH)
                                .build();

    GitCommandExecutionResponse response = (GitCommandExecutionResponse) ngGitCommandTask.run(params);
    assertThat(response.getGitCommandStatus()).isEqualTo(GitCommandStatus.SUCCESS);
    assertThat(response.getGitCommandResult()).isEqualTo(gitCommitAndPushResult);
  }

  @Test
  @Owner(developers = ABHINAV2)
  @Category(UnitTests.class)
  public void testUnsupportedGitOperation() {
    TaskParameters params = GitCommandParams.builder()
                                .gitConfig(GitConfigDTO.builder().build())
                                .gitCommandType(GitCommandType.COMMIT)
                                .build();

    GitCommandExecutionResponse response = (GitCommandExecutionResponse) ngGitCommandTask.run(params);
    assertThat(response.getGitCommandStatus()).isEqualTo(GitCommandStatus.FAILURE);
  }
}