/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.delegatetasks.cv;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import software.wings.service.impl.analysis.DataCollectionInfoV2;
@TargetModule(HarnessModule._930_DELEGATE_TASKS)
public interface DataCollector<T extends DataCollectionInfoV2> {
  void init(DataCollectionExecutionContext dataCollectionExecutionContext, T dataCollectionInfo)
      throws DataCollectionException;
  int getHostBatchSize();
}
