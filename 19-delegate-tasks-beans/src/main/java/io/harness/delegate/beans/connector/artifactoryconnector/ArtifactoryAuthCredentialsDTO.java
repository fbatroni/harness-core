package io.harness.delegate.beans.connector.artifactoryconnector;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.harness.beans.DecryptableEntity;
import io.swagger.annotations.ApiModel;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY, visible = true)
@ApiModel("ArtifactoryAuthCredentials")
public interface ArtifactoryAuthCredentialsDTO extends DecryptableEntity {}
