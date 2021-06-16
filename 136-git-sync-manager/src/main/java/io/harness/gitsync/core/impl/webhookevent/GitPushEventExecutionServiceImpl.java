package io.harness.gitsync.core.impl.webhookevent;

import static io.harness.gitsync.common.WebhookEventConstants.GIT_PUSH_EVENT;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.eventsframework.webhookpayloads.webhookdata.WebhookDTO;
import io.harness.gitsync.common.beans.BranchSyncStatus;
import io.harness.gitsync.common.beans.YamlChangeSetEventType;
import io.harness.gitsync.common.service.GitBranchService;
import io.harness.gitsync.common.service.YamlGitConfigService;
import io.harness.gitsync.core.beans.GitWebhookRequestAttributes;
import io.harness.gitsync.core.dtos.YamlChangeSetDTO;
import io.harness.gitsync.core.dtos.YamlChangeSetSaveDTO;
import io.harness.gitsync.core.service.YamlChangeSetService;
import io.harness.gitsync.core.service.webhookevent.GitPushEventExecutionService;
import io.harness.product.ci.scm.proto.ParseWebhookResponse;
import io.harness.product.ci.scm.proto.Repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.DX)
@Singleton
@Slf4j
public class GitPushEventExecutionServiceImpl implements GitPushEventExecutionService {
  @Inject YamlGitConfigService yamlGitConfigService;
  @Inject YamlChangeSetService yamlChangeSetService;
  @Inject GitBranchService gitBranchService;

  @Override
  public void processEvent(WebhookDTO webhookDTO) {
    try {
      ParseWebhookResponse scmParsedWebhookResponse = webhookDTO.getParsedResponse();
      if (scmParsedWebhookResponse == null || scmParsedWebhookResponse.getPush() == null) {
        log.error("{} : Error while consuming webhook Parsed response : {}", GIT_PUSH_EVENT, webhookDTO);
        return;
      }

      Repository repository = scmParsedWebhookResponse.getPush().getRepo();

      if (Boolean.TRUE.equals(yamlGitConfigService.isRepoExists(repository.getLink()))) {
        // if unsynced branch exists in this repo, then ignore the event
        if (gitBranchService.isBranchExists(webhookDTO.getAccountId(), repository.getLink(),
                getBranchName(scmParsedWebhookResponse), BranchSyncStatus.UNSYNCED)) {
          log.info("{} : Branch {} exists in UNSYNCED state, ignoring the event : {}", GIT_PUSH_EVENT,
              repository.getBranch(), webhookDTO);
        } else {
          // create queue event and pass it to the git queue
          YamlChangeSetDTO yamlChangeSetDTO = yamlChangeSetService.save(prepareQueueEvent(webhookDTO));
          log.info("{} : Yaml change set queue event id {} created for webhook event id : {}", GIT_PUSH_EVENT,
              yamlChangeSetDTO.getChangesetId(), webhookDTO.getEventId());
        }
      } else {
        log.info("{} : Repository doesn't exist, ignoring the event : {}", GIT_PUSH_EVENT, repository);
      }
    } catch (Exception exception) {
      log.error("{} : Exception while processing webhook event : {}", GIT_PUSH_EVENT, webhookDTO, exception);
    }
  }

  // ------------------------- PRIVATE METHODS --------------------------

  private YamlChangeSetSaveDTO prepareQueueEvent(WebhookDTO webhookDTO) {
    Repository repository = webhookDTO.getParsedResponse().getPush().getRepo();
    String commitId = webhookDTO.getParsedResponse().getPush().getCommit().getSha();
    final String branchName = getBranchName(webhookDTO.getParsedResponse());
    return YamlChangeSetSaveDTO.builder()
        .accountId(webhookDTO.getAccountId())
        .branch(branchName)
        .repoUrl(repository.getLink())
        .eventType(YamlChangeSetEventType.BRANCH_PUSH)
        .gitWebhookRequestAttributes(GitWebhookRequestAttributes.builder()
                                         .branchName(branchName)
                                         .repo(repository.getLink())
                                         .webhookBody(webhookDTO.getJsonPayload())
                                         .webhookHeaders(webhookDTO.getHeadersList().toString())
                                         .headCommitId(commitId)
                                         .build())
        .build();
  }

  private String getBranchName(ParseWebhookResponse parseWebhookResponse) {
    String branchRef = parseWebhookResponse.getPush().getRef();
    final int lastIndexOfSlash = branchRef.lastIndexOf('/');
    return branchRef.substring(lastIndexOfSlash + 1);
  }
}
