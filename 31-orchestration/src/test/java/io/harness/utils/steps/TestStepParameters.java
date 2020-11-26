package io.harness.utils.steps;

import io.harness.state.io.StepParameters;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;

@Data
@Builder
@TypeAlias("testStepParameters25")
public class TestStepParameters implements StepParameters {
  String param;
}
