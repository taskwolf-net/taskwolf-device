package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileDeleteRequest extends PacketOutgoing {
  private final UUID deleteId;
  private final String deviceId;
  private final String filePath;
  private final String fileName;

  public PacketOutgoingFileDeleteRequest(
    UUID deleteId, String deviceId, String filePath, String fileName
  ) {
    super(0x30);
    this.deleteId = deleteId;
    this.deviceId = deviceId;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(deleteId);
    buffer.writeString(deviceId);
    buffer.writeString(filePath);
    buffer.writeString(fileName);
  }
}
