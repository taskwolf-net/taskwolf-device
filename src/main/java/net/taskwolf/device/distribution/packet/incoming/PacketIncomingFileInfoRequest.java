package net.taskwolf.device.distribution.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoRequest extends PacketIncoming {
  private UUID infoId;
  private String deviceId;
  private String path;

  public PacketIncomingFileInfoRequest() {
    super(0x28);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
    deviceId = buffer.readString();
    path = buffer.readString();
  }
}
