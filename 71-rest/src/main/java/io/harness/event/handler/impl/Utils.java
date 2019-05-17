package io.harness.event.handler.impl;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.event.handler.impl.Constants.EMAIL_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.event.model.marketo.Error;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.Account;
import software.wings.beans.User;
import software.wings.service.intfc.UserService;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author rktummala
 */
@Singleton
@Slf4j
public class Utils {
  @Inject private UserService userService;

  public String getUserInviteUrl(String email, Account account) throws URISyntaxException {
    if (account != null) {
      return userService.getUserInviteUrl(email, account);
    } else {
      return userService.getUserInviteUrl(email);
    }
  }

  public String getFirstName(String name, String email) {
    if (isEmpty(name) || name.equals(email)) {
      return null;
    }

    String[] words = name.split(" ");
    int numberOfWords = words.length;
    if (numberOfWords == 1 || numberOfWords == 2) {
      return words[0];
    } else {
      return name.substring(0, name.lastIndexOf(words[numberOfWords - 1]) - 1);
    }
  }

  public String getLastName(String name, String email) {
    if (isEmpty(name) || name.equals(email)) {
      return null;
    }

    String[] words = name.split(" ");
    int numberOfWords = words.length;
    if (numberOfWords == 1) {
      return words[0];
    } else {
      return words[numberOfWords - 1];
    }
  }

  public String getDaysLeft(long expiryTime) {
    long delta = expiryTime - System.currentTimeMillis();
    if (delta <= 0) {
      return "0";
    }

    return "" + delta / Duration.ofDays(1).toMillis();
  }

  public String getErrorMsg(List<Error> errors) {
    if (isEmpty(errors)) {
      return "No error msg reported in response";
    }

    Error error = errors.get(0);
    StringBuilder builder = new StringBuilder(32);
    return builder.append("error code is: ")
        .append(error.getCode())
        .append(" , error msg is: ")
        .append(error.getMessage())
        .toString();
  }

  public User getUser(Map<String, String> properties) {
    String email = properties.get(EMAIL_ID);
    if (isEmpty(email)) {
      logger.error("User email is empty");
      return null;
    }

    User user = userService.getUserByEmail(email);
    if (user == null) {
      logger.error("User not found for email {}", email);
      return null;
    }

    List<Account> accounts = user.getAccounts();
    if (isEmpty(accounts)) {
      logger.info("User {} is not assigned to any accounts", email);
      return null;
    } else {
      if (accounts.size() > 1) {
        // At this point, only harness users can be assigned to more than one account.
        // Many systems like Marketo and Salesforce follow the model one user - one account.
        logger.info("User {} is associated with more than one account, skipping segment publish", email);
        return null;
      }
    }

    return user;
  }
}
