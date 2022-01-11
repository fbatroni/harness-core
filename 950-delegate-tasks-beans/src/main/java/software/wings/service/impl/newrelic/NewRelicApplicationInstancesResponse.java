/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package software.wings.service.impl.newrelic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Created by rsingh on 8/29/17.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NewRelicApplicationInstancesResponse {
  private List<NewRelicApplicationInstance> application_instances;
}
