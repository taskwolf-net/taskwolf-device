package net.taskwolf.device.distribution.device.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingDeviceLogout extends PacketIncoming {
  private String deviceId;

  public PacketIncomingDeviceLogout() {
    super(0x21);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    deviceId = buffer.readString();
  }
}
