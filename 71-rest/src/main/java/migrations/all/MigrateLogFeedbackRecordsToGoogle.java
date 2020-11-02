package migrations.all;

import com.google.inject.Inject;

import io.harness.beans.PageRequest;
import io.harness.beans.PageRequest.PageRequestBuilder;
import io.harness.persistence.HIterator;
import lombok.extern.slf4j.Slf4j;
import migrations.Migration;
import org.mongodb.morphia.query.Query;
import software.wings.dl.WingsPersistence;
import software.wings.service.impl.GoogleDataStoreServiceImpl;
import software.wings.service.impl.analysis.LogMLFeedbackRecord;
import software.wings.service.intfc.DataStoreService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MigrateLogFeedbackRecordsToGoogle implements Migration {
  @Inject private WingsPersistence wingsPersistence;
  @Inject private DataStoreService dataStoreService;

  @Override
  public void migrate() {
    if (dataStoreService instanceof GoogleDataStoreServiceImpl) {
      deleteAllElements();
      List<LogMLFeedbackRecord> recordsFromMongo = new ArrayList<>();

      Query<LogMLFeedbackRecord> feedbackRecordQuery = wingsPersistence.createQuery(LogMLFeedbackRecord.class);
      try (HIterator<LogMLFeedbackRecord> records = new HIterator<>(feedbackRecordQuery.fetch())) {
        while (records.hasNext()) {
          recordsFromMongo.add(records.next());
        }
      }

      dataStoreService.save(LogMLFeedbackRecord.class, recordsFromMongo, true);
      log.info("Saved {} records from Mongo for LogMLFeedbackRecords in GDS", recordsFromMongo.size());
    }
  }

  private void deleteAllElements() {
    PageRequest<LogMLFeedbackRecord> logMLFeedbackRecordPageRequest = PageRequestBuilder.aPageRequest().build();

    List<LogMLFeedbackRecord> records =
        dataStoreService.list(LogMLFeedbackRecord.class, logMLFeedbackRecordPageRequest);
    records.forEach(feedbackRecord -> {
      if (!feedbackRecord.getUuid().startsWith("urn:uuid")) {
        dataStoreService.delete(LogMLFeedbackRecord.class, feedbackRecord.getUuid());
      }
    });
    log.info("Deleted {} records from GDS for LogMLFeedbackRecords", records.size());
  }
}
