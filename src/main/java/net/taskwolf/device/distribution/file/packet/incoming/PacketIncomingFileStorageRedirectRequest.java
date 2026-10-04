package net.taskwolf.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileStorageRedirectRequest extends PacketIncoming {
  private UUID storageId;

  public PacketIncomingFileStorageRedirectRequest() {
    super(0x32);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    storageId = buffer.readUUID();
  }
}
