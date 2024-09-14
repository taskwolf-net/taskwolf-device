package com.dulno.device.distribution.device.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

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