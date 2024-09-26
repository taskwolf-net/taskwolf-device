package com.dulno.device.distribution.file.packet.incoming;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoRedirectResponse extends PacketIncoming {
  private UUID infoId;
  private String redirectUrl;

  public PacketIncomingFileInfoRedirectResponse() {
    super(0x41);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
    redirectUrl = buffer.readString();
  }
}
