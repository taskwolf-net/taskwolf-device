package net.taskwolf.device.trigger.notification;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerContentDatabaseTable;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceNotificationTrigger implements Trigger {
  public static DeviceNotificationTrigger create(
    InputComponentSelect deviceComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    return new DeviceNotificationTrigger(deviceComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_device_notification", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-notification-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("device.trigger.notification.name")
      .withDescription("device.trigger.notification.description")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.notification.input.device.name",
        "device", "device.trigger.notification.input.device.description", deviceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.notification.title", "notificationTitle"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.notification.body", "notificationBody"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("device");
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(
      content.get("device")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
