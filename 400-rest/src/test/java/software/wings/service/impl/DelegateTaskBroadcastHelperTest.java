package software.wings.service.impl;

import static io.harness.beans.DelegateTask.Status.QUEUED;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.delegate.beans.TaskData.DEFAULT_ASYNC_CALL_TIMEOUT;
import static io.harness.rule.OwnerRule.JENNY;
import static io.harness.rule.OwnerRule.MARKO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;

import io.harness.beans.Cd1SetupFields;
import io.harness.beans.DelegateTask;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.task.http.HttpTaskParameters;
import io.harness.ff.FeatureFlagService;
import io.harness.rule.Owner;

import software.wings.WingsBaseTest;
import software.wings.beans.DelegateTaskBroadcast;

import com.google.inject.Inject;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.wings.beans.TaskType;

import java.util.ArrayList;

public class DelegateTaskBroadcastHelperTest extends WingsBaseTest {
  @Mock private BroadcasterFactory broadcasterFactory;
  @Mock private FeatureFlagService featureFlagService;
  @InjectMocks @Inject private DelegateTaskBroadcastHelper broadcastHelper;

  @Test
  @Owner(developers = MARKO)
  @Category(UnitTests.class)
  public void testRebroadcastDelegateTask() {
    DelegateTask delegateTask = DelegateTask.builder()
                                    .version(generateUuid())
                                    .accountId(generateUuid())
                                    .uuid(generateUuid())
                                    .data(TaskData.builder().async(true).build())
                                    .build();

    Broadcaster broadcaster = mock(Broadcaster.class);
    when(broadcasterFactory.lookup(anyString(), eq(true))).thenReturn(broadcaster);

    delegateTask.setPreAssignedDelegateId(null);
    delegateTask.setAlreadyTriedDelegates(null);

    broadcastHelper.rebroadcastDelegateTask(delegateTask);

    ArgumentCaptor<DelegateTaskBroadcast> argumentCaptor = ArgumentCaptor.forClass(DelegateTaskBroadcast.class);
    verify(broadcaster, times(1)).broadcast(argumentCaptor.capture());

    DelegateTaskBroadcast delegateTaskBroadcast = argumentCaptor.getValue();
    assertThat(delegateTaskBroadcast).isNotNull();
    assertThat(delegateTaskBroadcast.getVersion()).isEqualTo(delegateTask.getVersion());
    assertThat(delegateTaskBroadcast.getAccountId()).isEqualTo(delegateTask.getAccountId());
    assertThat(delegateTaskBroadcast.getTaskId()).isEqualTo(delegateTask.getUuid());
    assertThat(delegateTaskBroadcast.isAsync()).isEqualTo(delegateTask.getData().isAsync());
    assertThat(delegateTaskBroadcast.getPreAssignedDelegateId()).isEqualTo(delegateTask.getPreAssignedDelegateId());
  }



}
