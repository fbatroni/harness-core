package io.harness.plancreator.steps;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.common.SwaggerConstants;
import io.harness.data.validator.EntityIdentifier;
import io.harness.data.validator.EntityName;
import io.harness.pms.yaml.ParameterField;
import io.harness.pms.yaml.YamlNode;
import io.harness.validator.NGRegexValidatorConstants;
import io.harness.when.beans.StepWhenCondition;
import io.harness.yaml.core.StepSpecType;
import io.harness.yaml.core.failurestrategy.FailureStrategyConfig;
import io.harness.yaml.core.timeout.Timeout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.TypeAlias;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("stepElementConfig")
@OwnedBy(CDC)
// TODO this should go to yaml commons
@TargetModule(HarnessModule._884_PMS_COMMONS)
public class StepElementConfig {
  @JsonProperty(YamlNode.UUID_FIELD_NAME)
  @Getter(onMethod_ = { @ApiModelProperty(hidden = true) })
  @ApiModelProperty(hidden = true)
  String uuid;

  @NotNull @EntityIdentifier @Pattern(regexp = NGRegexValidatorConstants.IDENTIFIER_PATTERN) String identifier;
  @NotNull @EntityName @Pattern(regexp = NGRegexValidatorConstants.NAME_PATTERN) String name;
  String description;
  @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH)
  @Pattern(regexp = NGRegexValidatorConstants.TIMEOUT_PATTERN)
  ParameterField<Timeout> timeout;
  List<FailureStrategyConfig> failureStrategies;

  @Getter(onMethod_ = { @ApiModelProperty(hidden = true) })
  @ApiModelProperty(hidden = true)
  ParameterField<String> skipCondition;

  StepWhenCondition when;

  @NotNull String type;
  @JsonProperty("spec")
  @JsonTypeInfo(use = NAME, property = "type", include = EXTERNAL_PROPERTY, visible = true)
  StepSpecType stepSpecType;

  @Getter(onMethod_ = { @ApiModelProperty(hidden = true) })
  @ApiModelProperty(hidden = true)
  ParameterField<List<String>> delegateSelectors;

  @Builder
  public StepElementConfig(String uuid, String identifier, String name, String description,
      ParameterField<Timeout> timeout, List<FailureStrategyConfig> failureStrategies, String type,
      StepSpecType stepSpecType, ParameterField<String> skipCondition, StepWhenCondition when,
      ParameterField<List<String>> delegateSelectors) {
    this.uuid = uuid;
    this.identifier = identifier;
    this.name = name;
    this.description = description;
    this.timeout = timeout;
    this.failureStrategies = failureStrategies;
    this.type = type;
    this.stepSpecType = stepSpecType;
    this.skipCondition = skipCondition;
    this.delegateSelectors = delegateSelectors;
    this.when = when;
  }
}
