package software.wings.search.entities.related;
import static io.harness.data.structure.UUIDGenerator.generateUuid;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.harness.beans.EmbeddedUser;
import software.wings.audit.AuditHeader;
import software.wings.audit.EntityAuditRecord;
import software.wings.beans.HttpMethod;
import software.wings.search.framework.changestreams.ChangeEvent;
import software.wings.search.framework.changestreams.ChangeEvent.ChangeEventBuilder;
import software.wings.search.framework.changestreams.ChangeType;

import java.util.Arrays;

public class RelatedAuditEntityTestUtils {
  public static AuditHeader createAuditHeader(
      String auditHeaderId, String accountId, String appId, String entityId, String entityType, String operationType) {
    EmbeddedUser user = EmbeddedUser.builder().name("testing").build();

    AuditHeader auditHeader =
        AuditHeader.Builder.anAuditHeader()
            .withUuid(auditHeaderId)
            .withUrl("http://localhost:9090/wings/catalogs")
            .withResourcePath("catalogs")
            .withRequestMethod(HttpMethod.GET)
            .withHeaderString(
                "Cache-Control=;no-cache,Accept=;*/*,Connection=;keep-alive,User-Agent=;Mozilla/5.0 (Macintosh; "
                + "Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 "
                + "Safari/537.36,Host=;localhost:9090,"
                + "Postman-Token=;bdd7280e-bfac-b0f1-9603-c7b0e55a74af,"
                + "Accept-Encoding=;"
                + "gzip, deflate, sdch,Accept-Language=;en-US,en;q=0.8,Content-Type=;application/json")
            .withRemoteHostName("0:0:0:0:0:0:0:1")
            .withRemoteHostPort(555555)
            .withRemoteIpAddress("0:0:0:0:0:0:0:1")
            .withLocalHostName("Ujjawals-MacBook-Pro.local")
            .withLocalIpAddress("192.168.0.110")
            .withCreatedAt(System.currentTimeMillis())
            .withCreatedBy(user)
            .build();
    auditHeader.setAccountId(accountId);
    auditHeader.setEntityAuditRecords(
        Arrays.asList(createEntityAuditRecord(appId, entityId, entityType, operationType)));
    return auditHeader;
  }

  public static EntityAuditRecord createEntityAuditRecord(
      String appId, String entityId, String entityType, String operationType) {
    return EntityAuditRecord.builder()
        .entityId(entityId)
        .entityName("name")
        .operationType(operationType)
        .affectedResourceOperation(operationType)
        .affectedResourceId(entityId)
        .affectedResourceType(entityType)
        .appId(appId)
        .build();
  }

  public static DBObject getAuditHeaderChanges(String appId, String auditEntityId, String operationType) {
    BasicDBObject entityAuditRecord = new BasicDBObject();
    entityAuditRecord.put("entityId", auditEntityId);
    entityAuditRecord.put("entityName", "name");
    entityAuditRecord.put("appId", appId);
    entityAuditRecord.put("appName", "appName");
    entityAuditRecord.put("affectedResourceOperation", operationType);
    entityAuditRecord.put("affectedResourceId", auditEntityId);
    entityAuditRecord.put("affectedResourceName", "name");

    BasicDBObject changes = new BasicDBObject();
    changes.put("entityAuditRecords", Arrays.asList(entityAuditRecord));
    return changes;
  }

  public static ChangeEvent createAuditHeaderChangeEvent(
      AuditHeader auditHeader, String appId, ChangeType changeType, String auditEntityId, String operationType) {
    ChangeEventBuilder changeEventBuilder = ChangeEvent.builder();
    changeEventBuilder = changeEventBuilder.token("token")
                             .uuid(generateUuid())
                             .entityType(AuditHeader.class)
                             .changeType(changeType)
                             .fullDocument(auditHeader);

    if (changeType.equals(ChangeType.UPDATE)) {
      changeEventBuilder = changeEventBuilder.changes(getAuditHeaderChanges(appId, auditEntityId, operationType));
    }
    return changeEventBuilder.build();
  }
}
