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
    return create(row.findCell(0).stringValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(), row.findCell(3).stringValue());
  }

  private final String id;
  private final UUID ownerId;
  private final String password;
  private final String device;
}
