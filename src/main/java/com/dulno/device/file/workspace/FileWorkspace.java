package com.dulno.device.file.workspace;

import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.database.DatabaseTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileWorkspace {
  public static FileWorkspace of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static FileWorkspace of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("device")).stringValue(),
      row.findCell(columns.indexOf("path")).stringValue());
  }

  private final UUID id;
  private final String device;
  private final String path;
}
