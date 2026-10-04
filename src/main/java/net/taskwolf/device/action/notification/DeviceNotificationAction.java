package net.taskwolf.device.action.notification;

import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.device.notification.NotificationFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceNotificationAction implements Action<DeviceNotificationActionExecutor> {
  public static DeviceNotificationAction create(
    InputComponentSelect deviceComponentSelect,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    NotificationFactory notificationFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("notificationTitle", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("notificationBody", DatabaseDataType.TEXT));
    return new DeviceNotificationAction(deviceComponentSelect, deviceDatabaseTable,
      userDeviceDatabaseTable, notificationFactory,
      ActionContentDatabaseTable.create(databaseConnection,
        databaseKeyspace, "action_device_notification", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-notification-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.notification.name")
      .withDescription("device.action.notification.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.notification.input.device.name",
        "device", "device.action.notification.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.notification.input.notification.title.name",
        "notificationTitle", "device.action.notification.input.notification.title.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.notification.input.notification.body.name",
        "notificationBody", "device.action.notification.input.notification.body.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.notification.title", "notificationTitle"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.notification.body", "notificationBody"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("device"), content.get("notificationTitle"),
      content.get("notificationBody")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(2).stringValue(),
        "notificationTitle", row.findCell(3).stringValue(),
        "notificationBody", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceNotificationActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceNotificationActionExecutor.create(deviceDatabaseTable,
        userDeviceDatabaseTable, notificationFactory,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
