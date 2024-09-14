package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileDeleteResponse extends PacketOutgoing {
  private final UUID deleteId;
  private final boolean success;

  public PacketOutgoingFileDeleteResponse(
    UUID deleteId, boolean success
  ) {
    super(0x31);
    this.deleteId = deleteId;
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(deleteId);
    buffer.raw().writeBoolean(success);
  }
}
