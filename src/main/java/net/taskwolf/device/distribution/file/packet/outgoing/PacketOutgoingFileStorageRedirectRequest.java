package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileStorageRedirectRequest extends PacketOutgoing {
  private final UUID storageId;

  public PacketOutgoingFileStorageRedirectRequest(UUID storageId) {
    super(0x32);
    this.storageId = storageId;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
  }
}
