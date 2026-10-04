package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileInfoResponse extends PacketOutgoing {
  private final UUID infoId;
  private final boolean success;

  public PacketOutgoingFileInfoResponse(UUID infoId, boolean success) {
    super(0x29);
    this.infoId = infoId;
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.raw().writeBoolean(success);
  }
}
