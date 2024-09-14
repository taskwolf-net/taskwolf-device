package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

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
    content = new byte[buffer.readVarInt()];
    buffer.raw().readBytes(content);
    success = buffer.raw().readBoolean();
  }
}
