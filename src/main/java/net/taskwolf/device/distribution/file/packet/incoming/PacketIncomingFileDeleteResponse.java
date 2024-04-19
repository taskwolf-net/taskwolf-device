package net.taskwolf.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileDeleteResponse extends PacketIncoming {
  private UUID deleteId;
  private boolean success;

  public PacketIncomingFileDeleteResponse() {
    super(0x31);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    deleteId = buffer.readUUID();
    success = buffer.raw().readBoolean();
  }
}
