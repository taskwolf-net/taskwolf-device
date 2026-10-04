package net.taskwolf.device.distribution.command.packet.incoming;

import net.taskwolf.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingCommandRequest extends PacketIncoming {
  private UUID commandId;
  private String deviceId;
  private DevicePlatform devicePlatform;
  private String command;

  public PacketIncomingCommandRequest() {
    super(0x24);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    commandId = buffer.readUUID();
    deviceId = buffer.readString();
    devicePlatform = DevicePlatform.valueOf(buffer.readString());
    command = buffer.readString();
  }
}

