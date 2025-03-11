package com.dulno.device.structure;

import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DeviceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device";

  public static DeviceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("machine", DatabaseDataType.TEXT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("information", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("platform", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("language", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("workflowNotifications",
      DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("errorNotifications",
      DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("newsNotifications",
      DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("commandExecution",
      DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("fileStorage", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("fileInfo", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("fileDelete", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("folderCreate", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("folderDelete", DatabaseDataType.BOOLEAN));
    var table = new DeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("machine");
    table.createIndexIfNotExists("owner");
    table.initializeViews();
    return table;
  }

  private DatabaseTable machineOwnerView;
  private final Random random = new Random();

  private DeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("machine", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.TEXT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    machineOwnerView = createMaterializedViewIfNotExists("machine_owner_view",
      columns);
  }

  public CompletableFuture<Void> insertDevice(Device device) {
    return insertDevice(device.id(), device.machineId(), device.ownerId(),
      device.information(), device.platform().toString(), device.language(),
      device.workflowNotifications(), device.errorNotifications(),
      device.newsNotifications(), device.commandExecution(),
      device.fileStorage(), device.fileInfo(), device.fileDelete(),
      device.folderCreate(), device.folderDelete());
  }

  public CompletableFuture<Void> insertDevice(
    String id, String machineId, UUID ownerId, String information,
    String platform, String language, boolean workflowNotifications,
    boolean errorNotifications, boolean newsNotifications,
    boolean commandExecution, boolean fileStorage, boolean fileInfo,
    boolean fileDelete, boolean folderCreate, boolean folderDelete
  ) {
    return insert(DatabaseRow.of(id, machineId, ownerId, information, platform,
      language, workflowNotifications, errorNotifications, newsNotifications,
      commandExecution, fileStorage, fileInfo, fileDelete, folderCreate,
      folderDelete));
  }

  public CompletableFuture<Void> renameDevice(Device device, String newName) {
    device.renameDevice(newName);
    return updateDevice(device);
  }

  public CompletableFuture<Void> changeDeviceOwner(Device device, UUID owner) {
    device.updateOwner(owner);
    return updateDevice(device);
  }

  public CompletableFuture<Void> updateDeviceLanguage(Device device, String language) {
    device.updateLanguage(language);
    return updateDevice(device);
  }

  public CompletableFuture<Void> updateDeviceNotificationSettings(
    Device device, boolean workflowNotifications, boolean errorNotifications,
    boolean newsNotifications
  ) {
    device.updateNotificationSettings(workflowNotifications, errorNotifications,
      newsNotifications);
    return updateDevice(device);
  }

  public CompletableFuture<Void> updateDeviceCommandSettings(
    Device device, boolean commandExecution
  ) {
    device.updateCommandSettings(commandExecution);
    return updateDevice(device);
  }

  public CompletableFuture<Void> updateDeviceFileSettings(
    Device device, boolean fileStorage, boolean fileInfo, boolean fileDelete,
    boolean folderCreate, boolean folderDelete
  ) {
    device.updateFileSettings(fileStorage, fileInfo, fileDelete,
      folderCreate, folderDelete);
    return updateDevice(device);
  }

  private CompletableFuture<Void> updateDevice(Device device) {
    return update(DatabaseCondition.of("id", device.id(), "machine", device.machineId()),
      DatabaseRow.of(device.id(), device.machineId(), device.ownerId(),
        device.information(), device.platform().toString(), device.language(),
        device.workflowNotifications(), device.errorNotifications(),
        device.newsNotifications(), device.commandExecution(),
        device.fileStorage(), device.fileInfo(), device.fileDelete(),
        device.folderCreate(), device.folderDelete()));
  }

  public CompletableFuture<Void> deleteDevice(String deviceId) {
    return delete(DatabaseCondition.of("id", deviceId));
  }

  public CompletableFuture<String> generateAvailableDeviceId() {
    var futureResponse = new CompletableFuture<String>();
    var id = createDeviceId();
    deviceExists(id).thenApply(exists -> exists ?
      generateAvailableDeviceId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

  private String createDeviceId() {
    var value = new StringBuilder();
    for (int i = 0; i < 32; i++) {
      value.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return value.toString();
  }

  public CompletableFuture<Boolean> deviceExists(String deviceId) {
    return exists(DatabaseCondition.of("id", deviceId));
  }

  public CompletableFuture<Boolean> deviceExists(String machineId, UUID ownerId) {
    return machineOwnerView.exists(DatabaseCondition.of("owner", ownerId,
      "machine", machineId));
  }

  public CompletableFuture<Device> findDevice(String deviceId) {
    return selectRow(DatabaseCondition.of("id", deviceId))
      .thenApply(row -> Device.of(row, this));
  }

  public CompletableFuture<Device> findDevice(String machineId, UUID ownerId) {
    return machineOwnerView.selectRow(DatabaseCondition.of("owner", ownerId,
      "machine", machineId)).thenApply(row -> Device.of(row, machineOwnerView));
  }

  public CompletableFuture<List<Device>> findDevicesOfOwner(UUID ownerId) {
    return selectRows(DatabaseCondition.of("owner", ownerId))
      .thenApply(rows -> rows.stream().map(row -> Device.of(row, this)).toList());
  }
}
