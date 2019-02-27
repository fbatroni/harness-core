package io.harness.limits.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.eraro.ErrorCode;
import io.harness.exception.WingsException;
import io.harness.limits.ActionType;
import io.harness.limits.ConfiguredLimit;
import io.harness.limits.defaults.service.DefaultLimitsService;
import io.harness.limits.impl.model.RateLimit;
import io.harness.limits.impl.model.StaticLimit;
import io.harness.limits.lib.Limit;
import io.harness.persistence.ReadPref;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.Account;
import software.wings.beans.AccountType;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.AccountService;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@Singleton
@ParametersAreNonnullByDefault
public class LimitConfigurationServiceMongo implements LimitConfigurationService {
  private static final Logger log = LoggerFactory.getLogger(LimitConfigurationServiceMongo.class);

  @Inject private WingsPersistence dao;
  @Inject private AccountService accountService;
  @Inject private DefaultLimitsService defaultLimits;

  @Override
  @Nullable
  public ConfiguredLimit getOrDefault(String accountId, ActionType actionType) {
    ConfiguredLimit limit = get(accountId, actionType);

    if (null == limit) {
      limit = getDefaultLimit(actionType, accountId);
    }

    return limit;
  }

  @VisibleForTesting
  @Nullable
  public ConfiguredLimit get(String accountId, ActionType actionType) {
    return dao.createQuery(ConfiguredLimit.class)
        .filter(ACCOUNT_ID, accountId)
        .filter("key", actionType.toString())
        .get();
  }

  @Nullable
  private ConfiguredLimit getDefaultLimit(ActionType actionType, String accountId) {
    Account account = accountService.get(accountId);
    Objects.requireNonNull(account, "account should not be null. AccountId: " + accountId);

    String accountType;
    if (null != account.getLicenseInfo()) {
      accountType = account.getLicenseInfo().getAccountType();
    } else {
      // LicenseInfo can be null for some accounts. We haven't set the license info for some old accounts yet.
      // Defaulting to PAID
      accountType = AccountType.PAID;
    }

    Preconditions.checkArgument(
        AccountType.isValid(accountType), "accountType should be valid. AccountId: " + accountId);

    Limit limit = defaultLimits.get(actionType, accountType.toUpperCase());
    if (null == limit) {
      log.error(
          "Default limit is null. Action Type: {}, Account Type: {}. Please configure the same in DefaultLimitsImpl class",
          actionType, accountType);
      return null;
    }

    return new ConfiguredLimit<>(accountId, limit, actionType);
  }

  @Override
  public boolean configure(String accountId, ActionType actionType, Limit limit) {
    if (StringUtils.isEmpty(accountId)) {
      throw new WingsException(ErrorCode.INVALID_ARGUMENT, "Account ID is empty");
    }

    ConfiguredLimit configuredLimit;

    switch (limit.getLimitType()) {
      case STATIC:
        configuredLimit = new ConfiguredLimit<>(accountId, (StaticLimit) limit, actionType);
        break;
      case RATE_LIMIT:
        RateLimit rateLimit = (RateLimit) limit;
        Objects.requireNonNull(rateLimit.getDurationUnit(), "durationUnit can't be null for a rate limit");
        configuredLimit = new ConfiguredLimit<>(accountId, rateLimit, actionType);
        break;
      default:
        throw new IllegalArgumentException("Unknown limit type: " + limit.getLimitType());
    }

    Datastore ds = dao.getDatastore(ConfiguredLimit.class, ReadPref.NORMAL);

    UpdateOperations<ConfiguredLimit> updateOp = ds.createUpdateOperations(ConfiguredLimit.class)
                                                     .set("accountId", configuredLimit.getAccountId())
                                                     .set("key", configuredLimit.getKey())
                                                     .set("limit", configuredLimit.getLimit());

    Query<ConfiguredLimit> query = ds.createQuery(ConfiguredLimit.class)
                                       .field("accountId")
                                       .equal(configuredLimit.getAccountId())
                                       .field("key")
                                       .equal(configuredLimit.getKey());

    UpdateOptions options = new UpdateOptions();
    options.upsert(true);

    UpdateResults results = ds.update(query, updateOp, options);
    if (results.getUpdatedCount() < 1 && results.getInsertedCount() < 1) {
      log.error(
          "Both inserted and updated count are less than 1. UpdateCount: {}, InsertCount: {}, Account ID: {}, actionType: {}, Limit: {}",
          results.getUpdatedCount(), results.getInsertedCount(), accountId, actionType, limit);
      return false;
    }

    return true;
  }

  @Override
  public void deleteByAccountId(String accountId) {
    dao.delete(dao.createQuery(ConfiguredLimit.class).filter(ACCOUNT_ID, accountId));
  }
}