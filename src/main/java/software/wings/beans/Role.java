package software.wings.beans;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

/**
 * Created by anubhaw on 3/16/16.
 */

@Entity(value = "roles", noClassnameStored = true)
public class Role extends Base {
  private String name;
  private String description;
  @Reference(idOnly = true) private List<Permission> permissions;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name.toUpperCase();
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public List<Permission> getPermissions() {
    return permissions;
  }
  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }
}
