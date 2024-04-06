package net.taskwolf.device.action;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceNotificationAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.notification.name")
      .withDescription("device.action.notification.description")
      .withIdentifier("device-notification-action")
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

  public static DeviceNotificationAction of(
    DeviceDatabaseTable deviceDatabaseTable,
    NotificationFactory notificationFactory, JSONObject content
  ) {
    return create(deviceDatabaseTable, notificationFactory,
      content.getString("device"), content.getString("notificationTitle"),
      content.getString("notificationBody"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final String deviceId;
  private String notificationTitle;
  private String notificationBody;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    notificationTitle = dissolve.dissolve(notificationTitle);
    notificationBody = dissolve.dissolve(notificationBody);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::publishNotification);
  }

  private CompletableFuture<ActionResult> publishNotification(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.notification.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::publishNotification);
  }

  private CompletableFuture<ActionResult> publishNotification(Device device) {
    notificationFactory.createNotification(device, notificationTitle,
      notificationBody).publish();
    return ActionResult.futureSuccess(buildInformation(device));
  }

  private Map<String, Object> buildInformation(Device device) {
    var information = Maps.<String, Object>newHashMap();
    information.put("deviceId", device.id());
    information.put("deviceName", device.information());
    information.put("devicePlatform", device.platform());
    information.put("notificationTitle", notificationTitle);
    information.put("notificationBody", notificationBody);
    return information;
  }
}
