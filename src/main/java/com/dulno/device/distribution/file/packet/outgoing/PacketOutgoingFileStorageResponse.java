package com.dulno.device.distribution.file.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileStorageResponse extends PacketOutgoing {
  private final UUID storageId;
  private final boolean success;

  public PacketOutgoingFileStorageResponse(
    UUID storageId, boolean success
  ) {
    super(0x27);
    this.storageId = storageId;
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
    buffer.raw().writeBoolean(success);
  }
}