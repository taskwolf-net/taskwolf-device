package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileStorageResponse extends PacketIncoming {
  private UUID storageId;
  private boolean success;

  public PacketIncomingFileStorageResponse() {
    super(0x27);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    storageId = buffer.readUUID();
    success = buffer.raw().readBoolean();
  }
}
