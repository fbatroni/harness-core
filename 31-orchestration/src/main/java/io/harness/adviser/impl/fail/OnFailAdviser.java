package io.harness.adviser.impl.fail;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.execution.status.NodeExecutionStatus.brokeStatuses;

import com.google.common.base.Preconditions;

import io.harness.adviser.Advise;
import io.harness.adviser.Adviser;
import io.harness.adviser.AdviserType;
import io.harness.adviser.AdvisingEvent;
import io.harness.adviser.NextStepAdvise;
import io.harness.annotations.Produces;
import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.state.io.StepResponse;

@OwnedBy(CDC)
@Redesign
@Produces(Adviser.class)
public class OnFailAdviser implements Adviser {
  public static final AdviserType ADVISER_TYPE = AdviserType.builder().type(AdviserType.ON_FAIL).build();

  @Override
  public Advise onAdviseEvent(AdvisingEvent advisingEvent) {
    StepResponse stepResponse = advisingEvent.getStepResponse();
    if (!brokeStatuses().contains(stepResponse.getStatus())) {
      return null;
    }
    OnFailAdviserParameters parameters =
        (OnFailAdviserParameters) Preconditions.checkNotNull(advisingEvent.getAdviserParameters());
    return NextStepAdvise.builder().nextNodeId(parameters.getNextNodeId()).build();
  }

  @Override
  public AdviserType getType() {
    return ADVISER_TYPE;
  }
}
