package net.taskwolf.device.structure;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

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
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("machine", DatabaseDataType.TEXT));
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
    return new DeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private final Random random = new Random();

  private DeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertDevice(Device device) {
    insertDevice(device.id(), device.machineId(), device.ownerId(),
      device.information(), device.platform().toString(), device.language(),
      device.workflowNotifications(), device.errorNotifications(),
      device.newsNotifications(), device.commandExecution(),
      device.fileStorage(), device.fileInfo());
  }

  public void insertDevice(
    String id, String machineId, UUID ownerId, String information,
    String platform, String language, boolean workflowNotifications,
    boolean errorNotifications, boolean newsNotifications,
    boolean commandExecution, boolean fileStorage, boolean fileInfo
  ) {
    insert(DatabaseRow.of(id, machineId, ownerId, information, platform,
      language, workflowNotifications, errorNotifications, newsNotifications,
      commandExecution, fileStorage, fileInfo));
  }

  public void changeDeviceOwner(Device device, UUID owner) {
    device.updateOwner(owner);
    updateDevice(device);
  }

  public void updateDeviceLanguage(Device device, String language) {
    device.updateLanguage(language);
    updateDevice(device);
  }

  public void updateDeviceNotificationSettings(
    Device device, boolean workflowNotifications, boolean errorNotifications,
    boolean newsNotifications
  ) {
    device.updateNotificationSettings(workflowNotifications, errorNotifications,
      newsNotifications);
    updateDevice(device);
  }

  public void updateDeviceCommandSettings(
    Device device, boolean commandExecution
  ) {
    device.updateCommandSettings(commandExecution);
    updateDevice(device);
  }

  public void updateDeviceFileSettings(
    Device device, boolean fileStorage, boolean fileInfo
  ) {
    device.updateFileSettings(fileStorage, fileInfo);
    updateDevice(device);
  }

  private void updateDevice(Device device) {
    update(DatabaseCell.create(device.id()), DatabaseRow.of(device.id(),
      device.machineId(), device.ownerId(), device.information(),
      device.platform().toString(), device.language(),
      device.workflowNotifications(), device.errorNotifications(),
      device.newsNotifications(), device.commandExecution(),
      device.fileStorage(), device.fileInfo()));
  }

  public void deleteDevice(String deviceId) {
    delete(DatabaseCell.create(deviceId));
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
    return exists(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<Boolean> deviceExists(String machineId, UUID ownerId) {
    return exists("machine='" + machineId + "' AND owner=" + ownerId + " ALLOW FILTERING");
  }

  public CompletableFuture<Device> findDevice(String deviceId) {
    return selectRow(DatabaseCell.create(deviceId)).thenApply(Device::of);
  }

  public CompletableFuture<Device> findDevice(String machineId, UUID ownerId) {
    return selectRow("machine='" + machineId + "' AND owner=" + ownerId +
      " ALLOW FILTERING").thenApply(Device::of);
  }

  public CompletableFuture<List<Device>> findDevicesOfOwner(UUID ownerId) {
    return selectRows("owner=" + ownerId + " ALLOW FILTERING").thenApply(rows ->
      rows.stream().map(Device::of).toList());
  }
}
