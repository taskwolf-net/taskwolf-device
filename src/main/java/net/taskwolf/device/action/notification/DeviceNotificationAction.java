package net.taskwolf.device.action.notification;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionContentDatabaseTable;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceNotificationAction implements Action<DeviceNotificationActionExecutor> {
  public static DeviceNotificationAction create(
    InputComponentSelect deviceComponentSelect,
    DeviceDatabaseTable deviceDatabaseTable, NotificationFactory notificationFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("notificationTitle", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("notificationBody", DatabaseDataType.TEXT));
    return new DeviceNotificationAction(deviceComponentSelect, deviceDatabaseTable,
      notificationFactory, ActionContentDatabaseTable.create(databaseConnection,
        databaseKeyspace, "action_device_notification", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
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
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), content.get("notificationTitle"),
      content.get("notificationBody")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "notificationTitle", row.findCell(2).stringValue(),
        "notificationBody", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceNotificationActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceNotificationActionExecutor.create(deviceDatabaseTable, notificationFactory,
        content.findCell(1).stringValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
