package io.harness.gitsync.gitBranch;

import static io.harness.rule.OwnerRule.HARI;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.gitsync.GitSyncTestBase;
import io.harness.gitsync.common.beans.BranchSyncStatus;
import io.harness.gitsync.common.beans.GitBranch;
import io.harness.gitsync.common.beans.YamlGitConfig;
import io.harness.gitsync.common.dtos.GitBranchDTO;
import io.harness.gitsync.common.dtos.GitBranchListDTO;
import io.harness.gitsync.common.impl.GitBranchServiceImpl;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;
import io.harness.repositories.gitBranches.GitBranchesRepository;
import io.harness.repositories.repositories.yamlGitConfig.YamlGitConfigRepository;
import io.harness.rule.Owner;
import io.harness.testlib.RealMongo;

import com.google.inject.Inject;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.DX)
public class GitBranchServiceImplTest extends GitSyncTestBase {
  @Inject GitBranchServiceImpl gitBranchServiceImpl;
  @Inject GitBranchesRepository gitBranchesRepository;
  @Inject YamlGitConfigRepository yamlGitConfigRepository;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = HARI)
  @RealMongo
  @Category(UnitTests.class)
  public void listBranchesWithStatusTest() {
    final String projectIdentifier = "projectId";
    final String orgIdentifier = "orgId";
    final String accountIdentifier = "accountId";
    final String yamlGitConfigIdentifier = "yamlGitConfigId";
    yamlGitConfigRepository.save(YamlGitConfig.builder()
                                     .accountId(accountIdentifier)
                                     .orgIdentifier(orgIdentifier)
                                     .projectIdentifier(projectIdentifier)
                                     .identifier(yamlGitConfigIdentifier)
                                     .repo("repoURL")
                                     .branch("A")
                                     .build());
    final GitBranch gitBranch1 = buildGitBranch(accountIdentifier, "repoURL", "Z", BranchSyncStatus.SYNCED);
    final GitBranch gitBranch2 = buildGitBranch(accountIdentifier, "repoURL", "C", BranchSyncStatus.SYNCING);
    final GitBranch gitBranch3 = buildGitBranch(accountIdentifier, "repoURL", "A", BranchSyncStatus.UNSYNCED);
    final GitBranch gitBranch4 = buildGitBranch(accountIdentifier, "repoURL", "B", BranchSyncStatus.UNSYNCED);
    gitBranchesRepository.saveAll(Arrays.asList(gitBranch1, gitBranch2, gitBranch3, gitBranch4));
    GitBranchListDTO gitBranchListDTO = gitBranchServiceImpl.listBranchesWithStatus(accountIdentifier, orgIdentifier,
        projectIdentifier, yamlGitConfigIdentifier, PageRequest.builder().pageIndex(0).pageSize(2).build(), "");
    PageResponse<GitBranchDTO> gitBranchPageResponse = gitBranchListDTO.getBranches();
    assertThat(!gitBranchPageResponse.isEmpty());
    assertThat(gitBranchPageResponse.getTotalItems() == 4 && gitBranchPageResponse.getTotalPages() == 2
        && gitBranchPageResponse.getPageItemCount() == 2
        && gitBranchPageResponse.getContent().get(0).getBranchSyncStatus() == BranchSyncStatus.SYNCED
        && gitBranchPageResponse.getContent().get(1).getBranchSyncStatus() == BranchSyncStatus.SYNCING);
  }

  private GitBranch buildGitBranch(
      String accountIdentifier, String repoURL, String branchName, BranchSyncStatus branchSyncStatus) {
    return GitBranch.builder()
        .accountIdentifier(accountIdentifier)
        .repoURL(repoURL)
        .branchName(branchName)
        .branchSyncStatus(branchSyncStatus)
        .build();
  }
}
