package com.dulno.device.distribution.command.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

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
