package io.harness.cvng.beans.appd;

import static io.harness.annotations.dev.HarnessTeam.CV;

import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.beans.DataCollectionRequestType;
import io.harness.cvng.beans.MetricPackDTO;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("APPDYNAMICS_GET_METRIC_DATA")
@OwnedBy(CV)
public class AppDynamicsMetricDataValidationRequest extends AppDynamicsDataCollectionRequest {
  public static final String DSL = AppDynamicsDataCollectionRequest.readDSL(
      "appd-metric-data.datacollection", AppDynamicsDataCollectionRequest.class);
  public AppDynamicsMetricDataValidationRequest() {
    setType(DataCollectionRequestType.APPDYNAMICS_GET_METRIC_DATA);
  }

  private String applicationName;
  private String tierName;
  private MetricPackDTO metricPack;

  @Override
  public String getDSL() {
    return DSL;
  }

  @Override
  public Map<String, Object> fetchDslEnvVariables() {
    Map<String, Object> dslEnvVariables = new HashMap<>();
    dslEnvVariables.put("applicationName", getApplicationName());
    dslEnvVariables.put("tierName", getTierName());
    List<String> metricPaths = new ArrayList<>();
    metricPack.getMetrics()
        .stream()
        .filter(metricDefinition -> metricDefinition.isIncluded())
        .map(metricDefinition -> metricDefinition.getValidationPath())
        .forEach(metricPath -> metricPaths.add(metricPath));
    dslEnvVariables.put("metricsToCollect", metricPaths);
    return dslEnvVariables;
  }
}
