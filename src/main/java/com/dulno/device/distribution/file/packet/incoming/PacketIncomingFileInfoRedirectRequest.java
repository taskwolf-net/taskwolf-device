package com.dulno.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoRedirectRequest extends PacketIncoming {
  private UUID infoId;

  public PacketIncomingFileInfoRedirectRequest() {
    super(0x40);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
  }
}
