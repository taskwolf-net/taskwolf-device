package net.taskwolf.device.distribution.device.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingDeviceLogin extends PacketOutgoing {
  private final String deviceId;

  public PacketOutgoingDeviceLogin(String deviceId) {
    super(0x20);
    this.deviceId = deviceId;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(deviceId);
  }
}
