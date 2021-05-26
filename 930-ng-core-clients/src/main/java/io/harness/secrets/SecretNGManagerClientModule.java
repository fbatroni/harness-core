package io.harness.secrets;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.secretmanagerclient.services.api.SecretManagerClientService;
import io.harness.secrets.remote.SecretNGManagerClient;
import io.harness.secrets.remote.SecretNGManagerHttpClientFactory;
import io.harness.secrets.services.NonPrivilegedSecretNGManagerClientServiceImpl;
import io.harness.secrets.services.PrivilegedSecretNGManagerClientServiceImpl;
import io.harness.security.ServiceTokenGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

@OwnedBy(PL)
public class SecretNGManagerClientModule extends AbstractModule {
  private final ServiceHttpClientConfig serviceHttpClientConfig;
  private final String serviceSecret;
  private final String clientId;

  public SecretNGManagerClientModule(
      ServiceHttpClientConfig serviceHttpClientConfig, String serviceSecret, String clientId) {
    this.serviceHttpClientConfig = serviceHttpClientConfig;
    this.serviceSecret = serviceSecret;
    this.clientId = clientId;
  }

  private SecretNGManagerHttpClientFactory privilegedSecretNGManagerHttpClientFactory() {
    return new SecretNGManagerHttpClientFactory(
        serviceHttpClientConfig, serviceSecret, new ServiceTokenGenerator(), null, clientId, ClientMode.PRIVILEGED);
  }

  private SecretNGManagerHttpClientFactory nonPrivilegedSecretNGManagerHttpClientFactory() {
    return new SecretNGManagerHttpClientFactory(
        serviceHttpClientConfig, serviceSecret, new ServiceTokenGenerator(), null, clientId, ClientMode.NON_PRIVILEGED);
  }

  @Override
  protected void configure() {
    bind(SecretNGManagerClient.class)
        .annotatedWith(Names.named(ClientMode.PRIVILEGED.name()))
        .toProvider(privilegedSecretNGManagerHttpClientFactory())
        .in(Scopes.SINGLETON);
    bind(SecretNGManagerClient.class).toProvider(nonPrivilegedSecretNGManagerHttpClientFactory()).in(Scopes.SINGLETON);

    bind(SecretManagerClientService.class).to(NonPrivilegedSecretNGManagerClientServiceImpl.class).in(Scopes.SINGLETON);
    bind(SecretManagerClientService.class)
        .annotatedWith(Names.named(ClientMode.PRIVILEGED.name()))
        .to(PrivilegedSecretNGManagerClientServiceImpl.class)
        .in(Scopes.SINGLETON);
    bind(SecretManagerClientService.class).to(NonPrivilegedSecretNGManagerClientServiceImpl.class).in(Scopes.SINGLETON);
  }
}
