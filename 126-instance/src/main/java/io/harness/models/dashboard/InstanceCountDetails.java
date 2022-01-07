/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.models.dashboard;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.environment.beans.EnvironmentType;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
@OwnedBy(HarnessTeam.DX)
public class InstanceCountDetails extends InstanceCountDetailsByEnvTypeBase {
  private List<InstanceCountDetailsByService> instanceCountDetailsByServiceList;

  public InstanceCountDetails(Map<EnvironmentType, Integer> envTypeVsInstanceCountMap,
      List<InstanceCountDetailsByService> instanceCountDetailsByServiceList) {
    super(envTypeVsInstanceCountMap);
    this.instanceCountDetailsByServiceList = instanceCountDetailsByServiceList;
  }
}
