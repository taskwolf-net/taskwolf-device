package com.dulno.device.distribution.command.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingCommandRequest extends PacketIncoming {
  private UUID commandId;
  private String deviceId;
  private String command;

  public PacketIncomingCommandRequest() {
    super(0x24);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    commandId = buffer.readUUID();
    deviceId = buffer.readString();
    command = buffer.readString();
  }
}

