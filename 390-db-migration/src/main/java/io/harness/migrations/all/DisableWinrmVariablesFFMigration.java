package io.harness.migrations.all;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.beans.FeatureName.DISABLE_WINRM_ENV_VARIABLES;
import static io.harness.beans.FeatureName.ENABLE_WINRM_ENV_VARIABLES;

import static java.lang.String.format;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.FeatureFlag;
import io.harness.beans.FeatureFlag.FeatureFlagKeys;
import io.harness.migrations.Migration;
import io.harness.persistence.HIterator;

import software.wings.beans.Account;
import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.UpdateOperations;

@OwnedBy(CDP)
@Slf4j
public class DisableWinrmVariablesFFMigration implements Migration {
  @Inject private WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    log.info("Starting the migration for WINRM_ENV_VARIABLES");
    FeatureFlag oldFeatureFlag = wingsPersistence.createQuery(FeatureFlag.class)
                                     .field(FeatureFlagKeys.name)
                                     .equal(DISABLE_WINRM_ENV_VARIABLES)
                                     .get();
    FeatureFlag newFeatureFlag = wingsPersistence.createQuery(FeatureFlag.class)
                                     .field(FeatureFlagKeys.name)
                                     .equal(ENABLE_WINRM_ENV_VARIABLES)
                                     .get();

    if (oldFeatureFlag != null && newFeatureFlag != null) {
      log.info(
          format("FF DISABLE_WINRM_ENV_VARIABLES is enabled for %d accounts", oldFeatureFlag.getAccountIds().size()));
      Set<String> accountIds = new HashSet<>();
      try (HIterator<Account> accounts =
               new HIterator<>(wingsPersistence.createQuery(Account.class).project(Account.ID_KEY2, true).fetch())) {
        for (Account account : accounts) {
          if (!oldFeatureFlag.getAccountIds().contains(account.getUuid())) {
            accountIds.add(account.getUuid());
          }
        }
        UpdateOperations<FeatureFlag> operations = wingsPersistence.createUpdateOperations(FeatureFlag.class);
        operations.set(FeatureFlagKeys.accountIds, accountIds);
        wingsPersistence.update(newFeatureFlag, operations);
        log.info(format("Enabled ENABLE_WINRM_ENV_VARIABLES for %d accounts", newFeatureFlag.getAccountIds().size()));
      }
    }
    log.info("Finished migration for WINRM_ENV_VARIABLES");
  }
}
