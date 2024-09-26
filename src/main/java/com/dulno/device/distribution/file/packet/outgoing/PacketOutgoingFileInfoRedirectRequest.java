package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileInfoRedirectRequest extends PacketOutgoing {
  private final UUID infoId;

  public PacketOutgoingFileInfoRedirectRequest(UUID infoId) {
    super(0x40);
    this.infoId = infoId;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
  }
}
