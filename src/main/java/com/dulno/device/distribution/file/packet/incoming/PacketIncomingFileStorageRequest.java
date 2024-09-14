package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileStorageRequest extends PacketIncoming {
  private UUID storageId;
  private String deviceId;
  private String filePath;
  private String fileName;
  private byte[] content;

  public PacketIncomingFileStorageRequest() {
    super(0x26);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    storageId = buffer.readUUID();
    deviceId = buffer.readString();
    filePath = buffer.readString();
    fileName = buffer.readString();
    content = new byte[buffer.readVarInt()];
    buffer.raw().readBytes(content);
  }
}
