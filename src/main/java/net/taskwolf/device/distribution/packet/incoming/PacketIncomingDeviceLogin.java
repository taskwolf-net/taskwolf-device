package net.taskwolf.device.distribution.packet.incoming;


import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingDeviceLogin extends PacketIncoming {
  private String deviceId;

  public PacketIncomingDeviceLogin() {
    super(0x20);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    deviceId = buffer.readString();
  }
}
