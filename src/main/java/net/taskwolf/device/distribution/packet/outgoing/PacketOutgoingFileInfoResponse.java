package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingFileInfoResponse extends PacketOutgoing {
  private final UUID infoId;
  private final byte[] content;
  private final boolean success;

  public PacketOutgoingFileInfoResponse(
    UUID infoId, byte[] content, boolean success
  ) {
    super(0x29);
    this.infoId = infoId;
    this.content = content;
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(infoId);
    buffer.writeVarInt(content.length);
    buffer.raw().writeBytes(content);
    buffer.raw().writeBoolean(success);
  }
}
