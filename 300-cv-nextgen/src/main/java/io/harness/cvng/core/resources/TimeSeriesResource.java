package io.harness.cvng.core.resources;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.annotations.ExposeInternalException;
import io.harness.cvng.analysis.beans.TimeSeriesTestDataDTO;
import io.harness.cvng.core.beans.TimeSeriesMetricDefinition;
import io.harness.cvng.core.services.api.TimeSeriesService;
import io.harness.rest.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Api("/timeseries")
@Path("/timeseries")
@Produces("application/json")
@ExposeInternalException
public class TimeSeriesResource {
  @Inject private TimeSeriesService timeSeriesService;

  @GET
  @Path("/metric-template")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get metric definition for a given data source config", nickname = "getMetricDefinitions")
  public RestResponse<List<TimeSeriesMetricDefinition>> getMetricDefinitions(
      @QueryParam("accountId") @NotNull String accountId, @QueryParam("cvConfigId") @NotNull String cvConfigId) {
    return new RestResponse<>(timeSeriesService.getTimeSeriesMetricDefinitions(cvConfigId));
  }

  // TODO: rename params to startTime and endTime instead of startTimeEpochMillis
  // The convention is to always use epoch millis for APIs. If something else is used
  // we need to specify that in the API.
  @GET
  @Path("/metric-group-data")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get time series data for a given data source config", nickname = "getTimeSeriesData")
  public RestResponse<TimeSeriesTestDataDTO> getTimeSeriesData(@QueryParam("accountId") @NotNull String accountId,
      @QueryParam("cvConfigId") @NotNull String cvConfigId,
      @QueryParam("startTimeEpochMillis") @NotNull Long startTimeEpochMillis,
      @QueryParam("endTimeEpochMillis") @NotNull Long endTimeEpochMillis,
      @QueryParam("metricName") @NotNull String metricName,
      @QueryParam("groupNameList") @NotNull List<String> groupNameList) {
    return new RestResponse<>(
        timeSeriesService.getMetricGroupDataForRange(cvConfigId, Instant.ofEpochMilli(startTimeEpochMillis),
            Instant.ofEpochMilli(endTimeEpochMillis), metricName, groupNameList));
  }

  @GET
  @Path("/anomalous-metric-data")
  @Timed
  @ExceptionMetered
  @ApiOperation(value = "get all anomalous metric timeseries data a given data source config",
      nickname = "getAnomalousMetricData")
  public RestResponse<TimeSeriesTestDataDTO>
  getAnomalousMetricData(@QueryParam("accountId") @NotNull String accountId,
      @QueryParam("cvConfigId") @NotNull String cvConfigId, @QueryParam("startTime") @NotNull Long startTimeEpochMillis,
      @QueryParam("endTime") @NotNull Long endTimeEpochMillis) {
    return new RestResponse<>(null);
  }
}
