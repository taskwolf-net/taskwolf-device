package net.taskwolf.device.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Device {
  public static Device of(DatabaseRow row) {
    return create(row.findCell(0).stringValue(), row.findCell(1).stringValue(),
      row.findCell(2).uuidValue(), row.findCell(3).stringValue(),
      DevicePlatform.valueOf(row.findCell(4).stringValue().toUpperCase()),
      row.findCell(5).booleanValue(), row.findCell(6).booleanValue(),
      row.findCell(7).booleanValue(), row.findCell(8).booleanValue());
  }

  private final String id;
  private final String machineId;
  private final UUID ownerId;
  private final String information;
  private final DevicePlatform platform;
  private final boolean workflowNotifications;
  private final boolean errorNotifications;
  private final boolean newsNotifications;
  private final boolean commandExecution;
}
