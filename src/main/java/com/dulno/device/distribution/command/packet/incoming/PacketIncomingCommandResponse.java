package com.dulno.device.distribution.command.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingCommandResponse extends PacketIncoming {
  private UUID commandId;
  private boolean delivered;
  private String output;
  private String errorMessage;
  private int exitCode;

  public PacketIncomingCommandResponse() {
    super(0x25);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    commandId = buffer.readUUID();
    delivered = buffer.raw().readBoolean();
    output = buffer.readString();
    errorMessage = buffer.readString();
    exitCode = buffer.readVarInt();
  }
}
