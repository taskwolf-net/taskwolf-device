package com.dulno.device.distribution.file.packet.incoming;

import com.dulno.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileDeleteRequest extends PacketIncoming {
  private UUID deleteId;
  private String deviceId;
  private DevicePlatform devicePlatform;
  private String filePath;
  private String fileName;

  public PacketIncomingFileDeleteRequest() {
    super(0x30);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    deleteId = buffer.readUUID();
    deviceId = buffer.readString();
    devicePlatform = DevicePlatform.valueOf(buffer.readString());
    filePath = buffer.readString();
    fileName = buffer.readString();
  }
}
