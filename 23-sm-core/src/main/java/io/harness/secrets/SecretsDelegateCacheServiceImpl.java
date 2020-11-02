package io.harness.secrets;

import static io.harness.annotations.dev.HarnessTeam.PL;

import com.google.inject.Singleton;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import io.harness.annotations.dev.OwnedBy;
import io.harness.security.encryption.SecretUniqueIdentifier;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.validation.executable.ValidateOnExecution;

@Singleton
@ValidateOnExecution
@OwnedBy(PL)
@Slf4j
public class SecretsDelegateCacheServiceImpl implements SecretsDelegateCacheService {
  private final Cache<SecretUniqueIdentifier, char[]> secretsCache;

  public SecretsDelegateCacheServiceImpl() {
    this.secretsCache = Caffeine.newBuilder()
                            .maximumWeight(2 * 1024 * 1024) // 4MB worth of characters
                            .expireAfterAccess(1, TimeUnit.HOURS)
                            .weigher(new SecretCacheWeigher())
                            .build();
  }

  public static class SecretCacheWeigher implements Weigher<SecretUniqueIdentifier, char[]> {
    @Override
    public int weigh(SecretUniqueIdentifier secretUniqueIdentifier, char[] value) {
      return value.length;
    }
  }

  @Override
  public char[] get(SecretUniqueIdentifier key, Function<SecretUniqueIdentifier, char[]> mappingFunction) {
    try {
      return secretsCache.get(key, mappingFunction);
    } catch (Exception e) {
      log.error("Cache get operation failed unexpectedly", e);
      return mappingFunction.apply(key);
    }
  }

  @Override
  public void put(SecretUniqueIdentifier key, char[] value) {
    try {
      secretsCache.put(key, value);
    } catch (Exception e) {
      log.error("Cache put operation failed unexpectedly", e);
    }
  }

  @Override
  public void remove(SecretUniqueIdentifier key) {
    try {
      secretsCache.invalidate(key);
    } catch (Exception e) {
      log.error("Cache invalidation failed for key {}", key, e);
    }
  }
}
