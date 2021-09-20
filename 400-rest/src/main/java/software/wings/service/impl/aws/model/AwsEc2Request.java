package software.wings.service.impl.aws.model;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.task.TaskParameters;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.AwsConfig;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TargetModule(HarnessModule._950_DELEGATE_TASKS_BEANS)
@OwnedBy(CDP)
public class AwsEc2Request extends AwsRequest implements TaskParameters {
  public enum AwsEc2RequestType {
    VALIDATE_CREDENTIALS,
    LIST_REGIONS,
    LIST_VPCS,
    LIST_SUBNETS,
    LIST_SGS,
    LIST_TAGS,
    LIST_INSTANCES
  }

  @NotNull private AwsEc2RequestType requestType;

  public AwsEc2Request(
      AwsConfig awsConfig, List<EncryptedDataDetail> encryptionDetails, AwsEc2RequestType requestType) {
    super(awsConfig, encryptionDetails);
    this.requestType = requestType;
  }
}
