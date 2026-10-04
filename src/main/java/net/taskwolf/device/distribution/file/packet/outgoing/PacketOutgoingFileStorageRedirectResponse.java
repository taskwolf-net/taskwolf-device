package net.taskwolf.device.distribution.file.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingFileStorageRedirectResponse extends PacketOutgoing {
  private final UUID storageId;
  private final String redirectUrl;

  public PacketOutgoingFileStorageRedirectResponse(
    UUID storageId, String redirectUrl
  ) {
    super(0x33);
    this.storageId = storageId;
    this.redirectUrl = redirectUrl;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(storageId);
    buffer.writeString(redirectUrl);
  }
}
