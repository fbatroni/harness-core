package software.wings.sm.states.pcf;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.EnvironmentType;
import io.harness.delegate.beans.pcf.CfRouteUpdateRequestConfigData;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.Application;
import software.wings.beans.PcfConfig;
import software.wings.beans.PcfInfrastructureMapping;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OwnedBy(CDP)
public class PcfRouteUpdateQueueRequestData {
  private Application app;
  private PcfConfig pcfConfig;
  private PcfInfrastructureMapping pcfInfrastructureMapping;
  private String activityId;
  private String envId;
  private EnvironmentType environmentType;
  private Integer timeoutIntervalInMinutes;
  private String commandName;
  private CfRouteUpdateRequestConfigData requestConfigData;
  private List<EncryptedDataDetail> encryptedDataDetails;
  private boolean skipRollback;
  private boolean downsizeOldApps;
  private boolean useCfCli;
}
