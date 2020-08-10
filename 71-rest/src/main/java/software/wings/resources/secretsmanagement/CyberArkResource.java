package software.wings.resources.secretsmanagement;

import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_ERROR;
import static software.wings.security.PermissionAttribute.PermissionType.MANAGE_SECRET_MANAGERS;
import static software.wings.security.PermissionAttribute.ResourceType.SETTING;

import com.google.inject.Inject;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.harness.logging.AccountLogContext;
import io.harness.logging.AutoLogContext;
import io.harness.rest.RestResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.CyberArkConfig;
import software.wings.security.annotations.AuthRule;
import software.wings.security.annotations.Scope;
import software.wings.service.intfc.security.CyberArkService;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author marklu on 2019-08-01
 */
@Api("cyberark")
@Path("/cyberark")
@Produces("application/json")
@Scope(SETTING)
@AuthRule(permissionType = MANAGE_SECRET_MANAGERS)
@Slf4j
public class CyberArkResource {
  @Inject private CyberArkService cyberArkService;

  @POST
  @Timed
  @ExceptionMetered
  public RestResponse<String> saveCyberArkConfig(
      @QueryParam("accountId") final String accountId, CyberArkConfig cyberArkConfig) {
    try (AutoLogContext ignore = new AccountLogContext(accountId, OVERRIDE_ERROR)) {
      logger.info("Adding CyberArk Secret Manager");
      return new RestResponse<>(cyberArkService.saveConfig(accountId, cyberArkConfig));
    }
  }

  @DELETE
  @Timed
  @ExceptionMetered
  public RestResponse<Boolean> deleteCyberArkConfig(
      @QueryParam("accountId") final String accountId, @QueryParam("configId") final String configId) {
    try (AutoLogContext ignore = new AccountLogContext(accountId, OVERRIDE_ERROR)) {
      logger.info("Deleting CyberArk Secret Manager");
      return new RestResponse<>(cyberArkService.deleteConfig(accountId, configId));
    }
  }
}
