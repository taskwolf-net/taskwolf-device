package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileInfoRedirectResponse extends PacketOutgoing {
  private final UUID infoId;
  private final String redirectUrl;

  public PacketOutgoingFileInfoRedirectResponse(UUID infoId, String redirectUrl) {
    super(0x41);
    this.infoId = infoId;
    this.redirectUrl = redirectUrl;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.writeString(redirectUrl);
  }
}
