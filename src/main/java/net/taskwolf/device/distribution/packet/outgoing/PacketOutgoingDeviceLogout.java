package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingDeviceLogout extends PacketOutgoing {
  private final String deviceId;

  public PacketOutgoingDeviceLogout(String deviceId) {
    super(0x21);
    this.deviceId = deviceId;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(deviceId);
  }
}