package software.wings.lock;

/**
 * Locker interface to acquire and release locks.
 *
 * @author Rishi
 */
public interface Locker {
  /**
   * Acquire lock.
   *
   * @param entityClass the entity class
   * @param entityId    the entity id
   * @return true, if successful
   */
  boolean acquireLock(Class entityClass, String entityId);

  /**
   * Acquire lock.
   *
   * @param entityClass the entity class
   * @param entityId    the entity id
   * @param timeout     timeout to acquire, in milliseconds
   * @return true, if successful
   */
  boolean acquireLock(Class entityClass, String entityId, long timeout);

  /**
   * Acquire lock.
   *
   * @param entityType the entity type
   * @param entityId   the entity id
   * @return true, if successful
   */
  boolean acquireLock(String entityType, String entityId);

  /**
   * Acquire lock.
   *
   * @param entityType the entity type
   * @param entityId   the entity id
   * @param timeout    timeout to acquire, in milliseconds
   * @return true, if successful
   */
  boolean acquireLock(String entityType, String entityId, long timeout);

  /**
   * Release lock.
   *
   * @param entityClass the entity class
   * @param entityId    the entity id
   * @return true, if successful
   */
  boolean releaseLock(Class entityClass, String entityId);

  /**
   * Release lock.
   *
   * @param entityType the entity type
   * @param entityId   the entity id
   * @return true, if successful
   */
  boolean releaseLock(String entityType, String entityId);
}
