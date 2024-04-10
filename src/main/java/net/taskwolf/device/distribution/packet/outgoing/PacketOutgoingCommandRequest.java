package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingCommandRequest extends PacketOutgoing {
  private final UUID commandId;
  private final String deviceId;
  private final String command;

  public PacketOutgoingCommandRequest(
    UUID commandId, String deviceId, String command
  ) {
    super(0x24);
    this.commandId = commandId;
    this.deviceId = deviceId;
    this.command = command;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(commandId);
    buffer.writeString(deviceId);
    buffer.writeString(command);
  }
}
