package net.taskwolf.device.distribution.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

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
