package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

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
