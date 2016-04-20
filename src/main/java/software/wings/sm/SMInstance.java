/**
 *
 */
package software.wings.sm;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Serialized;
import software.wings.beans.Base;

/**
 * @author Rishi
 */
@Entity(value = "smInstances", noClassnameStored = true)
public class SMInstance extends Base {
  private String stateMachineId;
  private String stateName;

  @Serialized private ExecutionContext context;
  private String parentInstanceId;
  private String cloneInstanceId;
  private String notifyId;
  private ExecutionStatus status = ExecutionStatus.NEW;

  private Long startTs;
  private Long endTs;

  public ExecutionContext getContext() {
    return context;
  }

  public void setContext(ExecutionContext context) {
    this.context = context;
  }

  public String getParentInstanceId() {
    return parentInstanceId;
  }

  public void setParentInstanceId(String parentInstanceId) {
    this.parentInstanceId = parentInstanceId;
  }

  public String getCloneInstanceId() {
    return cloneInstanceId;
  }

  public void setCloneInstanceId(String cloneInstanceId) {
    this.cloneInstanceId = cloneInstanceId;
  }

  public ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public String getNotifyId() {
    return notifyId;
  }

  public void setNotifyId(String notifyId) {
    this.notifyId = notifyId;
  }

  public String getStateMachineId() {
    return stateMachineId;
  }

  public void setStateMachineId(String stateMachineId) {
    this.stateMachineId = stateMachineId;
  }

  public Long getStartTs() {
    return startTs;
  }

  public void setStartTs(Long startTs) {
    this.startTs = startTs;
  }

  public Long getEndTs() {
    return endTs;
  }

  public void setEndTs(Long endTs) {
    this.endTs = endTs;
  }

  @PostLoad
  public void afterLoad() {
    context.setSmInstance(this);
  }
}
