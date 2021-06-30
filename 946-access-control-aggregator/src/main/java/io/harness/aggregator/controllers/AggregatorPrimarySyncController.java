package io.harness.aggregator.controllers;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.accesscontrol.acl.models.ACL;
import io.harness.accesscontrol.acl.repository.ACLRepository;
import io.harness.accesscontrol.principals.usergroups.UserGroupService;
import io.harness.accesscontrol.principals.usergroups.persistence.UserGroupRepository;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroupService;
import io.harness.accesscontrol.resources.resourcegroups.persistence.ResourceGroupRepository;
import io.harness.accesscontrol.roleassignments.persistence.repositories.RoleAssignmentRepository;
import io.harness.accesscontrol.roles.RoleService;
import io.harness.accesscontrol.roles.persistence.repositories.RoleRepository;
import io.harness.aggregator.AggregatorConfiguration;
import io.harness.aggregator.consumers.AccessControlDebeziumChangeConsumer;
import io.harness.aggregator.consumers.ChangeConsumerService;
import io.harness.aggregator.consumers.ChangeEventFailureHandler;
import io.harness.aggregator.models.MongoReconciliationOffset;
import io.harness.annotations.dev.OwnedBy;
import io.harness.lock.AcquiredLock;
import io.harness.lock.PersistentLocker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

@OwnedBy(PL)
@Singleton
@Slf4j
public class AggregatorPrimarySyncController extends AggregatorBaseSyncController implements Runnable {
  @Inject
  public AggregatorPrimarySyncController(@Named(ACL.PRIMARY_COLLECTION) ACLRepository primaryAclRepository,
      RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository,
      ResourceGroupRepository resourceGroupRepository, UserGroupRepository userGroupRepository, RoleService roleService,
      UserGroupService userGroupService, ResourceGroupService resourceGroupService,
      AggregatorConfiguration aggregatorConfiguration, PersistentLocker persistentLocker,
      ChangeEventFailureHandler changeEventFailureHandler, ChangeConsumerService changeConsumerService) {
    super(primaryAclRepository, roleAssignmentRepository, roleRepository, resourceGroupRepository, userGroupRepository,
        roleService, userGroupService, resourceGroupService, aggregatorConfiguration, persistentLocker,
        changeEventFailureHandler, AggregatorJobType.PRIMARY, changeConsumerService);
  }

  @Override
  public void run() {
    DebeziumEngine<ChangeEvent<String, String>> debeziumEngine = null;
    try (AcquiredLock<?> aggregatorLock = acquireLock(true)) {
      if (aggregatorLock == null) {
        return;
      }
      log.info("Acquired lock, initiating primary sync.");
      RLock rLock = (RLock) aggregatorLock.getLock();
      AccessControlDebeziumChangeConsumer accessControlDebeziumChangeConsumer = buildDebeziumChangeConsumer();
      debeziumEngine = getEngine(aggregatorConfiguration.getDebeziumConfig(), accessControlDebeziumChangeConsumer);
      Future<?> debeziumEngineFuture = executorService.submit(debeziumEngine);

      while (!debeziumEngineFuture.isDone() && rLock.isHeldByCurrentThread()) {
        log.info("primary lock remaining ttl {}, isHeldByCurrentThread {}, holdCount {}, name {}",
            rLock.remainTimeToLive(), rLock.isHeldByCurrentThread(), rLock.getHoldCount(), rLock.getName());
        TimeUnit.SECONDS.sleep(30);
      }
      log.warn("The primary sync debezium engine has unexpectedly stopped or the lock is no longer held");

    } catch (InterruptedException e) {
      log.warn("Thread interrupted, stopping primary aggregator sync", e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("Primary sync stopped due to exception", e);
    } finally {
      try {
        if (debeziumEngine != null) {
          debeziumEngine.close();
        }
      } catch (IOException exception) {
        log.error("Failed to close debezium engine", exception);
      }
    }
  }

  public String getLockName() {
    return String.format("%s_%s", ACCESS_CONTROL_AGGREGATOR_LOCK, AggregatorJobType.PRIMARY);
  }

  @Override
  protected String getOffsetStorageCollection() {
    return MongoReconciliationOffset.PRIMARY_COLLECTION;
  }
}
