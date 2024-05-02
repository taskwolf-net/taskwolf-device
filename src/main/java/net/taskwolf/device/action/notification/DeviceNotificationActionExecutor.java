package net.taskwolf.device.action.notification;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceNotificationActionExecutor implements ActionExecutor {
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
    if (!device.workflowNotifications()) {
      return ActionResult.futureFailure("device.action.notification.failure.device.permission");
    }
    notificationFactory.createNotification(device, notificationTitle,
      notificationBody).publish();
    return ActionResult.futureSuccess(buildInformation(device));
  }

  private Map<String, Object> buildInformation(Device device) {
    var information = device.composition();
    information.put("notificationTitle", notificationTitle);
    information.put("notificationBody", notificationBody);
    return information;
  }
}
