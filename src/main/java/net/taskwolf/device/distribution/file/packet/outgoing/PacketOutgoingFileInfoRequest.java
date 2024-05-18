package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileInfoRequest extends PacketOutgoing {
  private final UUID infoId;
  private final String deviceId;
  private final String filePath;
  private final String fileName;

  public PacketOutgoingFileInfoRequest(
    UUID infoId, String deviceId, String filePath, String fileName
  ) {
    super(0x28);
    this.infoId = infoId;
    this.deviceId = deviceId;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.writeString(deviceId);
    buffer.writeString(filePath);
    buffer.writeString(fileName);
  }
}
