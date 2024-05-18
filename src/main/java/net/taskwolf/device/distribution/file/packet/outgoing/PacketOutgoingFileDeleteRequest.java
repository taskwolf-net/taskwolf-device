package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
