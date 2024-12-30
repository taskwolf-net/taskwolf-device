package com.dulno.device.action.notification;

import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.dulno.device.structure.UserDeviceDatabaseTable;
import lombok.AllArgsConstructor;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.device.notification.NotificationFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceNotificationActionExecutor implements ActionExecutor {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final UUID ownerId;
  private final String deviceId;
  private String notificationTitle;
  private String notificationBody;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    notificationTitle = dissolve.dissolve(notificationTitle);
    notificationBody = dissolve.dissolve(notificationBody);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::checkDeviceExistence);
  }

  private CompletableFuture<ActionResult> checkDeviceExistence(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.notification.failure.device.not.found");
    }
    return userDeviceDatabaseTable.userHasDevice(ownerId, deviceId)
      .thenCompose(this::checkDeviceAccess);
  }

  private CompletableFuture<ActionResult> checkDeviceAccess(boolean hasPermission) {
    if (!hasPermission) {
      return ActionResult.futureFailure("device.action.notification.failure.device.access");
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
