package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

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