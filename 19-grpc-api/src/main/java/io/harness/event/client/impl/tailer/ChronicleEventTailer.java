package io.harness.event.client.impl.tailer;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import io.harness.event.EventPublisherGrpc.EventPublisherBlockingStub;
import io.harness.event.PublishMessage;
import io.harness.event.PublishRequest;
import io.harness.logging.LoggingListener;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.RollingChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Tails the chronicle-queue and publishes the events over rpc.
 */
@Slf4j
@Singleton
public class ChronicleEventTailer extends AbstractScheduledService {
  private static final String READ_TAILER = "read-tailer";
  private static final int MAX_COUNT = 5000;
  private static final int MAX_BYTES = 1024 * 1024; // 1MiB
  private static final Duration MIN_DELAY = Duration.ofMillis(200);
  private static final Duration MAX_DELAY = Duration.ofMinutes(5);

  private final EventPublisherBlockingStub blockingStub;
  private final ExcerptTailer readTailer;
  private final FileDeletionManager fileDeletionManager;
  private final BackoffScheduler scheduler;

  @Inject
  ChronicleEventTailer(EventPublisherBlockingStub blockingStub, @Named("tailer") RollingChronicleQueue chronicleQueue,
      FileDeletionManager fileDeletionManager) {
    this.blockingStub = blockingStub;
    chronicleQueue.refreshDirectlyListing();
    this.readTailer = chronicleQueue.createTailer(READ_TAILER);
    fileDeletionManager.setQueue(chronicleQueue);
    this.fileDeletionManager = fileDeletionManager;
    this.scheduler = new BackoffScheduler(MIN_DELAY, MAX_DELAY);
    addListener(new LoggingListener(this), MoreExecutors.directExecutor());
  }

  @Override
  protected void startUp() {
    logger.info("Starting up");
  }

  @Override
  protected void shutDown() {
    logger.info("Shutting down");
  }

  @Override
  protected void runOneIteration() {
    // service will terminate if exception is not caught.
    try {
      logger.trace("Checking for messages to publish");
      Batch batchToSend = new Batch(MAX_BYTES, MAX_COUNT);
      long prevIndex = getReadIndex();
      logger.trace("Read index: {}", prevIndex);
      while (!batchToSend.isFull()) {
        try (DocumentContext dc = readTailer.readingDocument()) {
          if (!dc.isPresent()) {
            logger.trace("Reached end of queue");
            break;
          }
          try {
            PublishMessage message = PublishMessage.parseFrom(requireNonNull(dc.wire()).read().bytes());
            batchToSend.add(message);
          } catch (Exception e) {
            logger.error("Exception while parsing message", e);
          }
        }
      }
      if (batchToSend.isFull()) {
        logger.trace("Batch is full");
      }
      if (!batchToSend.isEmpty()) {
        PublishRequest publishRequest = PublishRequest.newBuilder().addAllMessages(batchToSend.getMessages()).build();
        try {
          blockingStub.withDeadlineAfter(30, TimeUnit.SECONDS).publish(publishRequest);
          logger.debug("Published {} messages successfully", batchToSend.size());
          fileDeletionManager.setSentCycle(readTailer.cycle());
          scheduler.recordSuccess();
        } catch (Exception e) {
          logger.warn("Exception during message publish", e);
          readTailer.moveToIndex(prevIndex);
          scheduler.recordFailure();
        }
      } else {
        logger.trace("Skipping message publish as batch is empty");
      }
    } catch (Exception e) {
      logger.error("Encountered exception", e);
    }
  }

  @Override
  protected Scheduler scheduler() {
    return scheduler;
  }

  private long getReadIndex() {
    long index = readTailer.index();
    if (index == 0) {
      // For some reason, for a newly created tailer, tailer.index() returns 0 instead of the correct index, and the
      // rewind does not work. This is a workaround for that.
      readTailer.toStart();
      return readTailer.index();
    }
    return index;
  }
}
