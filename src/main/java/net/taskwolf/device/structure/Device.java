package net.taskwolf.device.structure;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.Map;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Device {
  public static Device of(DatabaseRow row) {
    return create(row.findCell(0).stringValue(), row.findCell(1).stringValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      DevicePlatform.valueOf(row.findCell(4).stringValue().toUpperCase()),
      row.findCell(5).stringValue(), row.findCell(6).booleanValue(),
      row.findCell(7).booleanValue(), row.findCell(8).booleanValue(),
      row.findCell(9).booleanValue(), row.findCell(10).booleanValue(),
      row.findCell(11).booleanValue(), row.findCell(12).booleanValue(),
      row.findCell(13).booleanValue(), row.findCell(14).booleanValue());
  }

  private final String id;
  private final String machineId;
  private UUID ownerId;
  private final String information;
  private final DevicePlatform platform;
  private String language;
  private boolean workflowNotifications;
  private boolean errorNotifications;
  private boolean newsNotifications;
  private boolean commandExecution;
  private boolean fileStorage;
  private boolean fileInfo;
  private boolean fileDelete;
  private boolean folderCreate;
  private boolean folderDelete;

  public void updateOwner(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public void updateLanguage(String language) {
    this.language = language;
  }

  public void updateNotificationSettings(
    boolean workflowNotifications, boolean errorNotifications,
    boolean newsNotifications
  ) {
    this.workflowNotifications = workflowNotifications;
    this.errorNotifications = errorNotifications;
    this.newsNotifications = newsNotifications;
  }

  public void updateCommandSettings(boolean commandExecution) {
    this.commandExecution = commandExecution;
  }

  public void updateFileSettings(
    boolean fileStorage, boolean fileInfo, boolean fileDelete,
    boolean folderCreate, boolean folderDelete
  ) {
    this.fileStorage = fileStorage;
    this.fileInfo = fileInfo;
    this.fileDelete = fileDelete;
    this.folderCreate = folderCreate;
    this.folderDelete = folderDelete;
  }

  public Map<String, Object> composition() {
    var information = Maps.<String, Object>newHashMap();
    information.put("deviceId", id);
    information.put("deviceName", this.information);
    information.put("devicePlatform", platform);
    return information;
  }
}
