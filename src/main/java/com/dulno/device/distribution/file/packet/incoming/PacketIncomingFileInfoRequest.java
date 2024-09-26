package com.dulno.device.distribution.file.packet.incoming;

import com.dulno.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoRequest extends PacketIncoming {
  private UUID infoId;
  private String deviceId;
  private DevicePlatform devicePlatform;
  private String filePath;
  private String fileName;

  public PacketIncomingFileInfoRequest() {
    super(0x28);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
    deviceId = buffer.readString();
    devicePlatform = DevicePlatform.valueOf(buffer.readString());
    filePath = buffer.readString();
    fileName = buffer.readString();
  }
}
