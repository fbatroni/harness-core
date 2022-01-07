/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package software.wings.yaml.directory;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import software.wings.yaml.YamlVersion.Type;

@OwnedBy(HarnessTeam.DX)
public class AccountLevelYamlNode extends YamlNode {
  public AccountLevelYamlNode() {}

  public AccountLevelYamlNode(
      String accountId, String uuid, String name, Class theClass, DirectoryPath directoryPath, Type type) {
    super(accountId, uuid, name, theClass, directoryPath, type);
  }
}
