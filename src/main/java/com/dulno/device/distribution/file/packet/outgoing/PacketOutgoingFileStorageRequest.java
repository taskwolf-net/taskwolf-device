package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.device.structure.DevicePlatform;

import java.util.UUID;

public final class PacketOutgoingFileStorageRequest extends PacketOutgoing {
  private final UUID storageId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String filePath;
  private final String fileName;
  private final byte[] content;

  public PacketOutgoingFileStorageRequest(
    UUID storageId, String deviceId, DevicePlatform devicePlatform,
    String filePath, String fileName, byte[] content
  ) {
    super(0x26);
    this.storageId = storageId;
    this.deviceId = deviceId;
    this.devicePlatform = devicePlatform;
    this.filePath = filePath;
    this.fileName = fileName;
    this.content = content;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
    buffer.writeString(deviceId);
    buffer.writeString(devicePlatform.toString());
    buffer.writeString(filePath);
    buffer.writeString(fileName);
    buffer.writeVarInt(content.length);
    buffer.raw().writeBytes(content);
  }
}

