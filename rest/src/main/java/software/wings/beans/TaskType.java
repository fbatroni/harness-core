package software.wings.beans;

import static org.joor.Reflect.on;

import software.wings.delegatetasks.BambooCollectionTask;
import software.wings.delegatetasks.CommandTask;
import software.wings.delegatetasks.DelegateRunnableTask;
import software.wings.delegatetasks.HttpTask;
import software.wings.delegatetasks.JenkinsCollectionTask;
import software.wings.delegatetasks.JenkinsTask;
import software.wings.delegatetasks.ServiceImplDelegateTask;
import software.wings.waitnotify.NotifyResponseData;

import java.util.function.Consumer;

/**
 * Created by peeyushaggarwal on 12/8/16.
 */
public enum TaskType {
  HTTP(HttpTask.class),
  SPLUNK(HttpTask.class),
  APP_DYNAMICS(HttpTask.class),
  JENKINS(JenkinsTask.class),
  JENKINS_COLLECTION(JenkinsCollectionTask.class),
  BAMBOO_COLLECTION(BambooCollectionTask.class),
  COMMAND(CommandTask.class),
  JENKINS_GET_BUILDS(ServiceImplDelegateTask.class),
  JENKINS_GET_JOBS(ServiceImplDelegateTask.class),
  JENKINS_GET_ARTIFACT_PATHS(ServiceImplDelegateTask.class),
  JENKINS_LAST_SUCCESSFUL_BUILD(ServiceImplDelegateTask.class),
  JENKINS_GET_PLANS(ServiceImplDelegateTask.class),
  BAMBOO_GET_BUILDS(ServiceImplDelegateTask.class),
  BAMBOO_GET_JOBS(ServiceImplDelegateTask.class),
  BAMBOO_GET_ARTIFACT_PATHS(ServiceImplDelegateTask.class),
  BAMBOO_LAST_SUCCESSFUL_BUILD(ServiceImplDelegateTask.class),
  BAMBOO_GET_PLANS(ServiceImplDelegateTask.class);

  private Class<? extends DelegateRunnableTask<?>> delegateRunnableTaskClass;

  TaskType(Class<? extends DelegateRunnableTask<?>> delegateRunnableTaskClass) {
    this.delegateRunnableTaskClass = delegateRunnableTaskClass;
  }

  public DelegateRunnableTask getDelegateRunnableTask(
      String delegateId, DelegateTask delegateTask, Consumer<? extends NotifyResponseData> consumer) {
    return on(delegateRunnableTaskClass).create(delegateId, delegateTask, consumer).get();
  }
}
