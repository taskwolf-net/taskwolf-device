package net.taskwolf.device.distribution.device.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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