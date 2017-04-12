package software.wings.api;

import software.wings.sm.StepExecutionSummary;

/**
 * Created by rishi on 4/4/17.
 */
public class CommandStepExecutionSummary extends StepExecutionSummary {
  private String serviceId;
  private String newContainerServiceName;
  private Integer newInstanceAddedCount;
  private String oldContainerServiceName;
  private Integer oldInstanceReducedCount;
  private String clusterName;

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getNewContainerServiceName() {
    return newContainerServiceName;
  }

  public void setNewContainerServiceName(String newContainerServiceName) {
    this.newContainerServiceName = newContainerServiceName;
  }

  public String getOldContainerServiceName() {
    return oldContainerServiceName;
  }

  public void setOldContainerServiceName(String oldContainerServiceName) {
    this.oldContainerServiceName = oldContainerServiceName;
  }

  public Integer getNewInstanceAddedCount() {
    return newInstanceAddedCount;
  }

  public void setNewInstanceAddedCount(Integer newInstanceAddedCount) {
    this.newInstanceAddedCount = newInstanceAddedCount;
  }

  public Integer getOldInstanceReducedCount() {
    return oldInstanceReducedCount;
  }

  public void setOldInstanceReducedCount(Integer oldInstanceReducedCount) {
    this.oldInstanceReducedCount = oldInstanceReducedCount;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }
}
