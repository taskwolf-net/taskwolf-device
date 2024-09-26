package com.dulno.device.distribution.command.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.device.structure.DevicePlatform;

import java.util.UUID;

public final class PacketOutgoingCommandRequest extends PacketOutgoing {
  private final UUID commandId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String command;

  public PacketOutgoingCommandRequest(
    UUID commandId, String deviceId, DevicePlatform devicePlatform, String command
  ) {
    super(0x24);
    this.commandId = commandId;
    this.deviceId = deviceId;
    this.devicePlatform = devicePlatform;
    this.command = command;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(commandId);
    buffer.writeString(deviceId);
    buffer.writeString(devicePlatform.toString());
    buffer.writeString(command);
  }
}
