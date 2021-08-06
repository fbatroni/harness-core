package io.harness.ngtriggers.buildtriggers.helpers;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.expression.EngineExpressionEvaluator;
import io.harness.jackson.JsonNodeUtils;
import io.harness.ngtriggers.beans.dto.TriggerDetails;
import io.harness.ngtriggers.beans.entity.NGTriggerEntity;
import io.harness.ngtriggers.buildtriggers.helpers.dtos.BuildTriggerOpsData;
import io.harness.pipeline.remote.PipelineServiceClient;
import io.harness.pms.merger.PipelineYamlConfig;
import io.harness.pms.merger.fqn.FQN;
import io.harness.pms.pipeline.PMSPipelineResponseDTO;
import io.harness.pms.yaml.YamlUtils;
import io.harness.polling.contracts.BuildInfo;
import io.harness.polling.contracts.PollingItem;
import io.harness.polling.contracts.PollingResponse;
import io.harness.remote.client.NGRestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;

@Singleton
@AllArgsConstructor(onConstructor = @__({ @Inject }))
@OwnedBy(PIPELINE)
public class BuildTriggerHelper {
  private PipelineServiceClient pipelineServiceClient;

  public Optional<String> fetchPipelineForTrigger(NGTriggerEntity ngTriggerEntity) {
    PMSPipelineResponseDTO response = NGRestUtils.getResponse(pipelineServiceClient.getPipelineByIdentifier(
        ngTriggerEntity.getTargetIdentifier(), ngTriggerEntity.getAccountId(), ngTriggerEntity.getOrgIdentifier(),
        ngTriggerEntity.getProjectIdentifier(), null, null, false));

    return response != null ? Optional.of(response.getYamlPipeline()) : Optional.empty();
  }

  public Map<String, JsonNode> fetchTriggerBuildSpecMap(NGTriggerEntity ngTriggerEntity) throws IOException {
    JsonNode jsonNode = YamlUtils.readTree(ngTriggerEntity.getYaml()).getNode().getCurrJsonNode();
    return JsonNodeUtils.getMap(jsonNode.get("trigger").get("source"), "spec");
  }

  public Map<String, Object> generateFinalMapWithBuildSpecFromPipeline(
      String pipeline, String stageRef, String buildRef, List<String> fqnDisplayStrs) {
    PipelineYamlConfig pipelineYamlConfig = new PipelineYamlConfig(pipeline);

    fqnDisplayStrs = fqnDisplayStrs.stream()
                         .map(str -> str.replace("STAGE_REF", stageRef).replace("BUILD_REF", buildRef))
                         .collect(toList());

    Map<String, Object> fqnToValueMap = new HashMap<>();
    for (Map.Entry<FQN, Object> entry : pipelineYamlConfig.getFqnToValueMap().entrySet()) {
      String key =
          fqnDisplayStrs.stream().filter(str -> entry.getKey().display().startsWith(str)).findFirst().orElse(null);
      if (key == null) {
        continue;
      }

      FQN mapKey = entry.getKey();
      String display = mapKey.display();
      fqnToValueMap.put(display.substring(key.length() + 1, display.length() - 1), entry.getValue());
    }

    return fqnToValueMap;
  }

  public void validateBuildType(BuildTriggerOpsData buildTriggerOpsData) {
    EngineExpressionEvaluator engineExpressionEvaluator = new EngineExpressionEvaluator(null);
    TextNode typeFromPipeline = (TextNode) buildTriggerOpsData.getPipelineBuildSpecMap().get("type");
    String typeFromTrigger =
        ((TextNode) engineExpressionEvaluator.evaluateExpression("type", buildTriggerOpsData.getTriggerSpecMap()))
            .asText();
    if (!typeFromPipeline.asText().equals(typeFromTrigger)) {
      throw new InvalidRequestException(String.format(
          "Artifact/Manifest Type in Trigger:{} does not match with Artifact/Manifest Type in Pipeline {}",
          typeFromTrigger, typeFromPipeline));
    }
  }

  public String fetchBuildType(Map<String, Object> buildTriggerSpecMap) {
    EngineExpressionEvaluator engineExpressionEvaluator = new EngineExpressionEvaluator(null);
    return ((TextNode) engineExpressionEvaluator.evaluateExpression("type", buildTriggerSpecMap)).asText();
  }

  public Map<String, Object> convertMapForExprEvaluation(Map<String, JsonNode> triggerSpecMap) {
    Map<String, Object> map = new HashMap<>();
    triggerSpecMap.forEach((k, v) -> map.put(k, v));
    return map;
  }

  public BuildTriggerOpsData generateBuildTriggerOpsDataForManifest(TriggerDetails triggerDetails, String pipelineYml)
      throws Exception {
    Map<String, JsonNode> triggerManifestSpecMap = fetchTriggerBuildSpecMap(triggerDetails.getNgTriggerEntity());

    String stageRef = triggerManifestSpecMap.get("stageIdentifier").asText();
    String buildRef = triggerManifestSpecMap.get("manifestRef").asText();
    List<String> keys = Arrays.asList(
        "pipeline.stages.stage[identifier:STAGE_REF].spec.serviceConfig.serviceDefinition.spec.manifests.manifest[identifier:BUILD_REF]",
        "pipeline.stages.parallel.stage[identifier:STAGE_REF].spec.serviceConfig.serviceDefinition.spec.manifests.manifest[identifier:BUILD_REF]");
    Map<String, Object> pipelineBuildSpecMap =
        generateFinalMapWithBuildSpecFromPipeline(pipelineYml, stageRef, buildRef, keys);

    Map<String, Object> manifestTriggerSpecMap = convertMapForExprEvaluation(triggerManifestSpecMap);
    return BuildTriggerOpsData.builder()
        .pipelineBuildSpecMap(pipelineBuildSpecMap)
        .triggerSpecMap(manifestTriggerSpecMap)
        .triggerDetails(triggerDetails)
        .build();
  }

  public BuildTriggerOpsData generateBuildTriggerOpsDataForArtifact(TriggerDetails triggerDetails, String pipelineYml)
      throws Exception {
    Map<String, JsonNode> triggerArtifactSpecMap = fetchTriggerBuildSpecMap(triggerDetails.getNgTriggerEntity());

    String stageRef = triggerArtifactSpecMap.get("stageIdentifier").asText();
    String buildRef = triggerArtifactSpecMap.get("artifactRef").asText();
    List<String> keys = Arrays.asList(
        "pipeline.stages.stage[identifier:STAGE_REF].spec.serviceConfig.serviceDefinition.spec.artifacts.primary",
        "pipeline.stages.parallel.stage[identifier:STAGE_REF].spec.serviceConfig.serviceDefinition.spec.artifacts.sidecars.sidecar[identifier:BUILD_REF]");
    Map<String, Object> pipelineBuildSpecMap =
        generateFinalMapWithBuildSpecFromPipeline(pipelineYml, stageRef, buildRef, keys);

    Map<String, Object> manifestTriggerSpecMap = convertMapForExprEvaluation(triggerArtifactSpecMap);
    return BuildTriggerOpsData.builder()
        .pipelineBuildSpecMap(pipelineBuildSpecMap)
        .triggerSpecMap(manifestTriggerSpecMap)
        .triggerDetails(triggerDetails)
        .build();
  }

  public String fetchStoreTypeForHelm(BuildTriggerOpsData buildTriggerOpsData) {
    EngineExpressionEvaluator engineExpressionEvaluator = new EngineExpressionEvaluator(null);
    return ((TextNode) engineExpressionEvaluator.evaluateExpression(
                "spec.store.type", buildTriggerOpsData.getTriggerSpecMap()))
        .asText();
  }

  public String fetchValueFromJsonNode(String path, Map<String, Object> map) {
    EngineExpressionEvaluator engineExpressionEvaluator = new EngineExpressionEvaluator(null);
    return ((TextNode) engineExpressionEvaluator.evaluateExpression(path, map)).asText();
  }

  public void validatePollingItemForHelmChart(PollingItem pollingItem) {
    String error = checkFiledValueError("ConnectorRef", pollingItem.getConnectorRef());
    if (isNotBlank(error)) {
      throw new InvalidRequestException(error);
    }

    if (pollingItem.getPayloadType().hasHttpHelmPayload()) {
      error = checkFiledValueError("ChartName", pollingItem.getPayloadType().getHttpHelmPayload().getChartName());
      if (isNotBlank(error)) {
        throw new InvalidRequestException(error);
      }

      error = checkFiledValueError(
          "helmVersion", pollingItem.getPayloadType().getHttpHelmPayload().getHelmVersion().name());
      if (isNotBlank(error)) {
        throw new InvalidRequestException(error);
      }
    } else {
      throw new InvalidRequestException("Store Type is not supported for HelmChart Trigger");
    }
  }

  public String checkFiledValueError(String fieldName, String fieldValue) {
    if (isBlank(fieldValue)) {
      return String.format("%s can not be blank. Needs to have concrete value", fieldName);
    } else if ("<+input>".equals(fieldValue)) {
      return String.format("%s can not be Runtime input in Trigger. Needs to have concrete value", fieldValue);
    } else {
      return EMPTY;
    }
  }

  public String generatePollingDescriptor(PollingResponse pollingResponse) {
    StringBuilder builder = new StringBuilder(1024);

    builder.append("AccountId: ").append(pollingResponse.getAccountId());

    if (pollingResponse.getSignaturesCount() > 0) {
      builder.append(", Signatures: [");
      for (int i = 0; i < pollingResponse.getSignaturesCount(); i++) {
        builder.append(pollingResponse.getSignatures(i)).append("  ");
      }
      builder.append("], ");
    }

    if (pollingResponse.hasBuildInfo()) {
      BuildInfo buildInfo = pollingResponse.getBuildInfo();

      builder.append(", BuildInfo Name: ").append(buildInfo.getName());
      builder.append(", Version: [");
      for (int i = 0; i < buildInfo.getVersionsCount(); i++) {
        builder.append(buildInfo.getVersions(i)).append("  ");
      }
      builder.append("]");
    }

    return builder.toString();
  }
}
