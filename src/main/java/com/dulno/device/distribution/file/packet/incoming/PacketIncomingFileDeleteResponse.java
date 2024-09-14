package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

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
