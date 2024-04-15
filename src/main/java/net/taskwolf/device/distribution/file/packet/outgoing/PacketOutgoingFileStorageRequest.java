package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileStorageRequest extends PacketOutgoing {
  private final UUID storageId;
  private final String deviceId;
  private final String filePath;
  private final String fileName;

  public PacketOutgoingFileStorageRequest(
    UUID storageId, String deviceId, String filePath, String fileName
  ) {
    super(0x26);
    this.storageId = storageId;
    this.deviceId = deviceId;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
    buffer.writeString(deviceId);
    buffer.writeString(filePath);
    buffer.writeString(fileName);
  }
}

