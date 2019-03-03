package io.harness.RestUtils;

import io.harness.framework.Retry;
import io.harness.framework.Setup;
import io.harness.framework.email.mailinator.MailinatorInbox;
import io.harness.framework.email.mailinator.MailinatorMessageDetails;
import io.harness.framework.email.mailinator.MailinatorMetaMessage;
import io.harness.framework.matchers.MailinatorEmailMatcher;

import java.util.List;

public class MailinatorRestUtils {
  final int MAX_RETRIES = 20;
  final int DELAY_IN_MS = 6000;
  final Retry<Object> retry = new Retry<>(MAX_RETRIES, DELAY_IN_MS);

  public MailinatorInbox retrieveInbox(String inboxName) {
    MailinatorInbox inbox = Setup.mailinator().queryParam("to", inboxName).get("/inbox").as(MailinatorInbox.class);

    return inbox;
  }

  public MailinatorMessageDetails readEmail(String inboxName, String emailFetchId) {
    MailinatorMessageDetails detailedMessage = Setup.mailinator()
                                                   .queryParam("to", inboxName)
                                                   .queryParam("id", emailFetchId)
                                                   .get("/email")
                                                   .as(MailinatorMessageDetails.class);

    return detailedMessage;
  }

  public MailinatorMessageDetails deleteEmail(String inboxName, String emailFetchId) {
    MailinatorMessageDetails detailedMessage = Setup.mailinator()
                                                   .queryParam("to", inboxName)
                                                   .queryParam("id", emailFetchId)
                                                   // .contentType("text/html;charset=utf-8")
                                                   .get("/delete")
                                                   .as(MailinatorMessageDetails.class);

    return detailedMessage;
  }

  public MailinatorMetaMessage retrieveMessageFromInbox(String inboxName, final String EXPECTED_SUBJECT) {
    MailinatorInbox inbox = (MailinatorInbox) retry.executeWithRetry(
        () -> retrieveInbox(inboxName), new MailinatorEmailMatcher<>(), EXPECTED_SUBJECT);
    List<MailinatorMetaMessage> messages = inbox.getMessages();
    MailinatorMetaMessage messageToReturn[] = new MailinatorMetaMessage[1];
    messages.forEach(message -> {
      if (message.getSubject().equals(EXPECTED_SUBJECT)) {
        messageToReturn[0] = message;
      }
    });
    return messageToReturn[0];
  }
}
