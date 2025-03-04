package com.dulno.device.structure;

import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.database.DatabaseTable;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class Device {
  public static Device of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static Device of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).stringValue(),
      row.findCell(columns.indexOf("machine")).stringValue(),
      row.findCell(columns.indexOf("owner")).uuidValue(),
      row.findCell(columns.indexOf("information")).stringValue(),
      DevicePlatform.valueOf(row.findCell(columns.indexOf("platform")).stringValue()),
      row.findCell(columns.indexOf("language")).stringValue(),
      row.findCell(columns.indexOf("workflowNotifications")).booleanValue(),
      row.findCell(columns.indexOf("errorNotifications")).booleanValue(),
      row.findCell(columns.indexOf("newsNotifications")).booleanValue(),
      row.findCell(columns.indexOf("commandExecution")).booleanValue(),
      row.findCell(columns.indexOf("fileStorage")).booleanValue(),
      row.findCell(columns.indexOf("fileInfo")).booleanValue(),
      row.findCell(columns.indexOf("fileDelete")).booleanValue(),
      row.findCell(columns.indexOf("folderCreate")).booleanValue(),
      row.findCell(columns.indexOf("folderDelete")).booleanValue());
  }

  private final String id;
  private final String machineId;
  private UUID ownerId;
  private String information;
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

  public void renameDevice(String newName) {
    this.information = newName;
  }

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
