package io.harness.connector.mappers.artifactorymapper;

import com.google.inject.Singleton;

import io.harness.connector.entities.embedded.artifactoryconnector.ArtifactoryConnector;
import io.harness.connector.entities.embedded.artifactoryconnector.ArtifactoryUserNamePasswordAuthentication;
import io.harness.connector.mappers.ConnectorEntityToDTOMapper;
import io.harness.connector.mappers.SecretRefHelper;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryAuthType;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryAuthenticationDTO;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryConnectorDTO;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryUsernamePasswordAuthDTO;

@Singleton
public class ArtifactoryEntityToDTO implements ConnectorEntityToDTOMapper<ArtifactoryConnector> {
  @Override
  public ArtifactoryConnectorDTO createConnectorDTO(ArtifactoryConnector artifactoryConnector) {
    ArtifactoryAuthenticationDTO artifactoryAuthenticationDTO = null;
    if (artifactoryConnector.getAuthType() != ArtifactoryAuthType.NO_AUTH
        || artifactoryConnector.getArtifactoryAuthentication() != null) {
      ArtifactoryUserNamePasswordAuthentication artifactoryCredentials =
          (ArtifactoryUserNamePasswordAuthentication) artifactoryConnector.getArtifactoryAuthentication();
      ArtifactoryUsernamePasswordAuthDTO artifactoryUsernamePasswordAuthDTO =
          ArtifactoryUsernamePasswordAuthDTO.builder()
              .username(artifactoryCredentials.getUsername())
              .passwordRef(SecretRefHelper.createSecretRef(artifactoryCredentials.getPasswordRef()))
              .build();
      artifactoryAuthenticationDTO = ArtifactoryAuthenticationDTO.builder()
                                         .authType(artifactoryConnector.getAuthType())
                                         .credentials(artifactoryUsernamePasswordAuthDTO)
                                         .build();
    }

    return ArtifactoryConnectorDTO.builder()
        .artifactoryServerUrl(artifactoryConnector.getUrl())
        .auth(artifactoryAuthenticationDTO)
        .build();
  }
}