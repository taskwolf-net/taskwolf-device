package net.taskwolf.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoResponse extends PacketIncoming {
  private UUID infoId;
  private byte[] content;
  private boolean success;

  public PacketIncomingFileInfoResponse() {
    super(0x29);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
    var length = buffer.readVarInt();
    content = buffer.raw().readBytes(length).array();
    success = buffer.raw().readBoolean();
  }
}
