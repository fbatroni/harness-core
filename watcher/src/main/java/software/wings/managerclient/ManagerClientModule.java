package software.wings.managerclient;

import com.google.inject.AbstractModule;

import io.harness.security.TokenGenerator;

public class ManagerClientModule extends AbstractModule {
  private String managerBaseUrl;
  private String accountId;
  private String accountSecret;

  public ManagerClientModule(String managerBaseUrl, String accountId, String accountSecret) {
    this.managerBaseUrl = managerBaseUrl;
    this.accountId = accountId;
    this.accountSecret = accountSecret;
  }

  @Override
  protected void configure() {
    TokenGenerator tokenGenerator = new TokenGenerator(accountId, accountSecret);
    bind(TokenGenerator.class).toInstance(tokenGenerator);
    bind(ManagerClient.class).toProvider(new ManagerClientFactory(managerBaseUrl, tokenGenerator));
  }
}
