package software.wings.resources;

import software.wings.beans.RestResponse;
import software.wings.beans.User;
import software.wings.security.PermissionAttribute.Action;
import software.wings.security.PermissionAttribute.PermissionType;
import software.wings.security.PermissionAttribute.ResourceType;
import software.wings.security.UserThreadLocal;
import software.wings.security.annotations.AuthRule;
import software.wings.security.annotations.DelegateAuth;
import software.wings.security.annotations.PublicApi;
import software.wings.security.annotations.Scope;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Created by anubhaw on 8/31/16.
 */
@Path("secure-resources")
@Produces("application/json")
public class SecureResource {
  /**
   * Public api response.
   *
   * @return the response
   */
  @GET
  @Path("publicApiAuthTokenNotRequired")
  @PublicApi
  public Response publicApi() {
    return Response.ok().build();
  }

  /**
   * Non public api rest response.
   *
   * @return the rest response
   */
  @GET
  @Path("NonPublicApi")
  @Scope(ResourceType.APPLICATION)
  @AuthRule(permissionType = PermissionType.LOGGED_IN)
  public RestResponse<User> NonPublicApi() {
    return new RestResponse<>(UserThreadLocal.get());
  }

  /**
   * App resource read action on app scope rest response.
   *
   * @return the rest response
   */
  @GET
  @Path("appResourceReadActionOnAppScope")
  @Scope(ResourceType.APPLICATION)
  @AuthRule(permissionType = PermissionType.ENV, action = Action.READ)
  public RestResponse<User> appResourceReadActionOnAppScope() {
    return new RestResponse<>(UserThreadLocal.get());
  }

  /**
   * App resource write action on app scope rest response.
   *
   * @return the rest response
   */
  @POST
  @Path("appResourceCreateActionOnAppScope")
  @Scope(ResourceType.APPLICATION)
  @AuthRule(permissionType = PermissionType.ENV, action = Action.CREATE)
  public RestResponse<User> appResourceCreateActionOnAppScope() {
    return new RestResponse<>(UserThreadLocal.get());
  }

  /**
   * Env resource read action on env scope rest response.
   *
   * @return the rest response
   */
  @GET
  @Path("envResourceReadActionOnEnvScope")
  @Scope(ResourceType.APPLICATION)
  @AuthRule(permissionType = PermissionType.ENV, action = Action.READ)
  public RestResponse<User> envResourceReadActionOnEnvScope() {
    return new RestResponse<>(UserThreadLocal.get());
  }

  /**
   * Env resource write action on env scope rest response.
   *
   * @return the rest response
   */
  @POST
  @Path("envResourceWriteActionOnEnvScope")
  @Scope(ResourceType.APPLICATION)
  @AuthRule(permissionType = PermissionType.ENV, action = Action.UPDATE)
  public RestResponse<User> envResourceWriteActionOnEnvScope() {
    return new RestResponse<>(UserThreadLocal.get());
  }

  @GET
  @Path("delegateAuth")
  @Scope(ResourceType.DELEGATE)
  @DelegateAuth
  public RestResponse<String> delegateAuth() {
    return new RestResponse<>("test");
  }
}
