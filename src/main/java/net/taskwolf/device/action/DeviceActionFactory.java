package net.taskwolf.device.action;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceActionFactory implements ActionFactory {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final NotificationFactory notificationFactory;

  @Override
  public Action create(String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("device-notification-action")) {
      return DeviceNotificationAction.of(deviceDatabaseTable,
        notificationFactory, json);
    }
    return null;
  }
}
