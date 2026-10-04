package net.taskwolf.device.structure;

import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class DeviceScan {
  public static DeviceScan of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static DeviceScan of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("creator")).uuidValue(),
      row.findCell(columns.indexOf("target")).uuidValue(),
      row.findCell(columns.indexOf("authToken")).uuidValue(),
      row.findCell(columns.indexOf("machine")).stringValue(),
      row.findCell(columns.indexOf("information")).stringValue(),
      row.findCell(columns.indexOf("platform")).stringValue(),
      row.findCell(columns.indexOf("firebaseToken")).stringValue(),
      row.findCell(columns.indexOf("scanned")).booleanValue(),
      row.findCell(columns.indexOf("approved")).booleanValue());
  }

  private final UUID id;
  private final UUID creatorId;
  private final UUID targetId;
  private final UUID token;
  private String machine;
  private String information;
  private String platform;
  private String firebaseToken;
  private boolean scanned;
  private boolean approved;

  public void scan(
    String machine, String information, String platform, String firebaseToken
  ) {
    this.machine = machine;
    this.information = information;
    this.platform = platform;
    this.firebaseToken = firebaseToken;
    this.scanned = true;
  }

  public void approve() {
    this.approved = true;
  }
}
