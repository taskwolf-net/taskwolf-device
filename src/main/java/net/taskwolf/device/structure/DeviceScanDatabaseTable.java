package net.taskwolf.device.structure;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DeviceScanDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device_scan";

  public static DeviceScanDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("authToken", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("machine", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("information", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("platform", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("firebaseToken", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("scanned", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("approved", DatabaseDataType.BOOLEAN));
    var table = new DeviceScanDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    return table;
  }

  private DeviceScanDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertScan(DeviceScan scan) {
    return insertScan(scan.id(), scan.creatorId(), scan.targetId(), scan.token(),
      scan.machine(), scan.information(), scan.platform(), scan.firebaseToken(),
      scan.scanned(), scan.approved());
  }

  public CompletableFuture<Void> insertScan(
    UUID id, UUID creatorId, UUID targetId, UUID token, String machine,
    String information, String platform, String firebaseToken, boolean scanned,
    boolean approved
  ) {
    return insert(DatabaseRow.of(id, creatorId, targetId, token, machine,
        information, platform, firebaseToken, scanned, approved),
      "USING TTL " + (60 * 5));
  }

  public CompletableFuture<Void> performScan(
    DeviceScan scan, String machine, String information, String platform,
    String firebaseToken
  ) {
    scan.scan(machine, information, platform, firebaseToken);
    return updateScan(scan);
  }

  public CompletableFuture<Void> approveScan(DeviceScan scan) {
    scan.approve();
    return updateScan(scan);
  }

  private CompletableFuture<Void> updateScan(DeviceScan scan) {
    return update(DatabaseCondition.of("id", scan.id()),
      DatabaseRow.of(scan.id(), scan.creatorId(), scan.targetId(), scan.token(),
        scan.machine(), scan.information(), scan.platform(), scan.firebaseToken(),
        scan.scanned(), scan.approved()), "USING TTL " + (60 * 5));
  }

  public CompletableFuture<Void> deleteScan(UUID scanId) {
    return delete(DatabaseCondition.of("id", scanId));
  }

  public CompletableFuture<UUID> generateAvailableScanId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    scanExists(id).thenApply(exists -> exists ?
      generateAvailableScanId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> scanExists(UUID scanId) {
    return exists(DatabaseCondition.of("id", scanId));
  }

  public CompletableFuture<DeviceScan> findScan(UUID scanId) {
    return selectRow(DatabaseCondition.of("id", scanId))
      .thenApply(row -> DeviceScan.of(row, this));
  }
}
