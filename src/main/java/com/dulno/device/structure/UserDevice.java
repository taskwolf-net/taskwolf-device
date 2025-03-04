package com.dulno.device.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.database.DatabaseRow;
import com.dulno.core.database.DatabaseTable;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class UserDevice {
  public static UserDevice of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static UserDevice of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("target")).uuidValue(),
      row.findCell(columns.indexOf("device")).stringValue(),
      row.findCell(columns.indexOf("owner")).uuidValue(),
      row.findCell(columns.indexOf("information")).stringValue(),
      DevicePlatform.valueOf(row.findCell(columns.indexOf("platform")).stringValue()));
  }

  private final UUID targetId;
  private final String deviceId;
  private UUID ownerId;
  private String information;
  private final DevicePlatform platform;

  public void renameDevice(String newName) {
    this.information = newName;
  }

  public void updateOwner(UUID ownerId) {
    this.ownerId = ownerId;
  }
}
