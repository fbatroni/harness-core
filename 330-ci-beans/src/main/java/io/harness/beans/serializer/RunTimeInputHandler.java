package io.harness.beans.serializer;

import static io.harness.common.NGExpressionUtils.matchesInputSetPattern;

import static java.lang.String.format;

import io.harness.exception.ngexception.CIStageExecutionUserException;
import io.harness.pms.yaml.ParameterField;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class RunTimeInputHandler {
  public static String UNRESOLVED_PARAMETER = "UNRESOLVED_PARAMETER";
  public boolean resolveGitClone(ParameterField<Boolean> cloneRepository) {
    if (cloneRepository == null || cloneRepository.isExpression() || cloneRepository.getValue() == null) {
      return true;
    } else {
      return (boolean) cloneRepository.fetchFinalValue();
    }
  }

  public boolean resolveBooleanParameter(ParameterField<Boolean> booleanParameterField, Boolean defaultValue) {
    if (booleanParameterField == null || booleanParameterField.isExpression()
        || booleanParameterField.getValue() == null) {
      if (defaultValue != null) {
        return defaultValue;
      } else {
        return false;
      }
    } else {
      return (boolean) booleanParameterField.fetchFinalValue();
    }
  }

  public String resolveStringParameter(String fieldName, String stepType, String stepIdentifier,
      ParameterField<String> parameterField, boolean isMandatory) {
    if (parameterField == null || parameterField.getValue() == null) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      } else {
        return UNRESOLVED_PARAMETER;
      }
    }

    // It only checks input set pattern. Variable can be resolved on lite engine.
    if (matchesInputSetPattern(parameterField.getValue())) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      } else {
        log.warn(format("Failed to resolve optional field %s in step type %s with identifier %s", fieldName, stepType,
            stepIdentifier));
        return UNRESOLVED_PARAMETER;
      }
    }

    return parameterField.getValue();
  }

  public Map<String, String> resolveMapParameter(String fieldName, String stepType, String stepIdentifier,
      ParameterField<Map<String, String>> parameterField, boolean isMandatory) {
    if (parameterField == null || parameterField.getValue() == null) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      } else {
        return null;
      }
    }

    if (parameterField.isExpression()) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      } else {
        log.warn(format("Failed to resolve optional field %s in step type %s with identifier %s", fieldName, stepType,
            stepIdentifier));
        return null;
      }
    }

    return parameterField.getValue();
  }

  public List<String> resolveListParameter(String fieldName, String stepType, String stepIdentifier,
      ParameterField<List<String>> parameterField, boolean isMandatory) {
    if (parameterField == null || parameterField.getValue() == null) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      }
    }

    if (parameterField.isExpression()) {
      if (isMandatory) {
        throw new CIStageExecutionUserException(
            format("Failed to resolve mandatory field %s in step type %s with identifier %s", fieldName, stepType,
                stepIdentifier));
      } else {
        log.warn(format("Failed to resolve optional field %s in step type %s with identifier %s", fieldName, stepType,
            stepIdentifier));
        return null;
      }
    }

    return parameterField.getValue();
  }
}
