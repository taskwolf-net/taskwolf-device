package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileInfoResponse extends PacketOutgoing {
  private final UUID infoId;
  private final boolean success;

  public PacketOutgoingFileInfoResponse(
    UUID infoId, boolean success
  ) {
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
