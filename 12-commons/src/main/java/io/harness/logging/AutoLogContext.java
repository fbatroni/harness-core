package io.harness.logging;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.govern.Switch.unhandled;
import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_ERROR;
import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AutoLogContext implements AutoCloseable {
  private List<MDCCloseable> handles;
  private Map<String, String> previous;
  private OverrideBehavior behavior;

  public enum OverrideBehavior { OVERRIDE_NESTS, OVERRIDE_ERROR }

  public AutoLogContext(Map<String, String> values, OverrideBehavior behavior) {
    this.behavior = behavior;
    for (Map.Entry<String, String> entry : values.entrySet()) {
      addKeyValue(entry.getKey(), entry.getValue());
    }
  }
  protected AutoLogContext(String key, String value, OverrideBehavior behavior) {
    this.behavior = behavior;
    addKeyValue(key, value);
  }

  private void addKeyValue(String key, String value) {
    final String original = MDC.get(key);
    if (original != null) {
      if (original.equals(value)) {
        // The context for this key is already set with the same value.
        // Do not initialize the key so we keep the context until the first initializer is hit.
        return;
      }

      switch (behavior) {
        case OVERRIDE_NESTS:
          if (previous == null) {
            previous = new HashMap<>();
          }
          previous.put(key, original);
          break;
        case OVERRIDE_ERROR:
          try (AutoLogContext ignore = new MdcKeyLogContext(key, OVERRIDE_ERROR)) {
            logger.error(
                format("Initialized in the same thread with a different value '%s'. Keeping the original value '%s'",
                    value, original),
                new Exception(""));
          }
          return;
        default:
          unhandled(behavior);
      }
    }
    if (handles == null) {
      handles = new ArrayList<>();
    }
    handles.add(MDC.putCloseable(key, value));
  }

  @Override
  public void close() {
    if (isNotEmpty(handles)) {
      handles.forEach(MDCCloseable::close);
    }
    if (isNotEmpty(previous)) {
      for (Map.Entry<String, String> entry : previous.entrySet()) {
        MDC.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
