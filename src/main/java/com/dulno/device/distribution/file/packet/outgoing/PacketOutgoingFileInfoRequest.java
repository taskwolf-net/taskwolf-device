package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.device.structure.DevicePlatform;

import java.util.UUID;

public final class PacketOutgoingFileInfoRequest extends PacketOutgoing {
  private final UUID infoId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String filePath;
  private final String fileName;

  public PacketOutgoingFileInfoRequest(
    UUID infoId, String deviceId, DevicePlatform devicePlatform,
    String filePath, String fileName
  ) {
    super(0x28);
    this.infoId = infoId;
    this.deviceId = deviceId;
    this.devicePlatform = devicePlatform;
    this.filePath = filePath;
    this.fileName = fileName;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.writeString(deviceId);
    buffer.writeString(devicePlatform.toString());
    buffer.writeString(filePath);
    buffer.writeString(fileName);
  }
}
