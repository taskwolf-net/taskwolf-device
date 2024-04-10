package net.taskwolf.device.distribution.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingCommandRequest extends PacketIncoming {
  private UUID commandId;
  private String deviceId;
  private String command;

  public PacketIncomingCommandRequest() {
    super(0x24);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    commandId = buffer.readUUID();
    deviceId = buffer.readString();
    command = buffer.readString();
  }
}

