/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.security;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * @author rktummala on 01/30/18
 */
@OwnedBy(PL)
@Data
@Builder
public class UserRestrictionInfo {
  private UsageRestrictions usageRestrictionsForUpdateAction;
  private Map<String, Set<String>> appEnvMapForUpdateAction;

  private UsageRestrictions usageRestrictionsForReadAction;
  private Map<String, Set<String>> appEnvMapForReadAction;
}
