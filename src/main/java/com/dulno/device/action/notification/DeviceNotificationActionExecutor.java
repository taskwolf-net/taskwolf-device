package com.dulno.device.action.notification;

import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import lombok.AllArgsConstructor;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import com.dulno.device.notification.NotificationFactory;

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
