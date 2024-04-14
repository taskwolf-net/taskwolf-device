package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileStorageRequest extends PacketOutgoing {
  private final UUID storageId;
  private final String deviceId;
  private final String path;
  private final byte[] content;

  public PacketOutgoingFileStorageRequest(
    UUID storageId, String deviceId, String path, byte[] content
  ) {
    super(0x26);
    this.storageId = storageId;
    this.deviceId = deviceId;
    this.path = path;
    this.content = content;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
    buffer.writeString(deviceId);
    buffer.writeString(path);
    buffer.writeVarInt(content.length);
    buffer.raw().writeBytes(content);
  }
}

