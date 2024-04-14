package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileInfoRequest extends PacketOutgoing {
  private final UUID infoId;
  private final String deviceId;
  private final String path;

  public PacketOutgoingFileInfoRequest(
    UUID infoId, String deviceId, String path
  ) {
    super(0x28);
    this.infoId = infoId;
    this.deviceId = deviceId;
    this.path = path;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.writeString(deviceId);
    buffer.writeString(path);
  }
}
