package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.device.structure.DevicePlatform;

import java.util.UUID;

public final class PacketOutgoingFileDeleteRequest extends PacketOutgoing {
  private final UUID deleteId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String filePath;
  private final String fileName;

  public PacketOutgoingFileDeleteRequest(
    UUID deleteId, String deviceId, DevicePlatform devicePlatform,
    String filePath, String fileName
  ) {
    super(0x30);
    this.deleteId = deleteId;
    this.deviceId = deviceId;
    this.devicePlatform = devicePlatform;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(deleteId);
    buffer.writeString(deviceId);
    buffer.writeString(devicePlatform.toString());
    buffer.writeString(filePath);
    buffer.writeString(fileName);
  }
}
