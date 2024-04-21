package net.taskwolf.device.action;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.device.command.CommandFactory;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceActionFactory implements ActionFactory {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final CommandFactory commandFactory;
  private final FileFactory fileFactory;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;

  @Override
  public Action create(String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("device-notification-action")) {
      return DeviceNotificationAction.of(deviceDatabaseTable,
        notificationFactory, json);
    }
    if (type.equals("device-command-action")) {
      return DeviceCommandAction.of(deviceDatabaseTable, commandFactory, json);
    }
    if (type.equals("device-file-store-action")) {
      return DeviceFileStoreAction.of(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, json);
    }
    if (type.equals("device-file-info-action")) {
      return DeviceFileInfoAction.of(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, json);
    }
    if (type.equals("device-file-delete-action")) {
      return DeviceFileDeleteAction.of(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, json);
    }
    if (type.equals("device-folder-create-action")) {
      return DeviceFolderCreateAction.of(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, json);
    }
    if (type.equals("device-folder-delete-action")) {
      return DeviceFolderDeleteAction.of(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, json);
    }
    return null;
  }
}
