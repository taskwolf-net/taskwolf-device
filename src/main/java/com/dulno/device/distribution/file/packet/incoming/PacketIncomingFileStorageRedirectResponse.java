package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileStorageRedirectResponse extends PacketIncoming {
  private UUID storageId;
  private String redirectUrl;

  public PacketIncomingFileStorageRedirectResponse() {
    super(0x33);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    storageId = buffer.readUUID();
    redirectUrl = buffer.readString();
  }
}
