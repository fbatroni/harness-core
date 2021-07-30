package io.harness.dtos;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.instancesync.ServerInstanceInfo;
import io.harness.dtos.deploymentinfo.DeploymentInfoDTO;
import io.harness.entities.ArtifactDetails;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@OwnedBy(HarnessTeam.DX)
@Getter
@Builder
public class DeploymentSummaryDTO {
  String id;
  String accountIdentifier;
  String orgIdentifier;
  String projectIdentifier;
  String pipelineExecutionId;
  String pipelineExecutionName;
  // TODO create dto for artifact details
  ArtifactDetails artifactDetails;
  String deployedById;
  String deployedByName;
  String infrastructureMappingId;
  @Nullable InfrastructureMappingDTO infrastructureMapping;
  // list of newly created server instances in fresh deployment
  List<ServerInstanceInfo> serverInstanceInfoList;
  DeploymentInfoDTO deploymentInfoDTO;
  long deployedAt;
  long createdAt;
  long lastModifiedAt;

  public void setServerInstanceInfoList(List<ServerInstanceInfo> serverInstanceInfoList) {
    this.serverInstanceInfoList = serverInstanceInfoList;
  }
}
