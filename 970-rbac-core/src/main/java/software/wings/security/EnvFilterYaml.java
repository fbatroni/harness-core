package software.wings.security;

import io.harness.annotations.dev.Module;
import io.harness.annotations.dev.TargetModule;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@TargetModule(Module._870_CG_YAML_BEANS)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnvFilterYaml extends FilterYaml {
  private List<String> filterTypes;

  @Builder
  public EnvFilterYaml(List<String> entityNames, List<String> filterTypes) {
    super(entityNames);
    this.filterTypes = filterTypes;
  }
}
