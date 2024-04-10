package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingCommandResponse extends PacketOutgoing {
  private final UUID commandId;
  private final boolean delivered;
  private final String output;
  private final String errorMessage;
  private final int exitCode;

  public PacketOutgoingCommandResponse(
    UUID commandId, boolean delivered, String output, String errorMessage,
    int exitCode
  ) {
    super(0x25);
    this.commandId = commandId;
    this.delivered = delivered;
    this.output = output;
    this.errorMessage = errorMessage;
    this.exitCode = exitCode;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(commandId);
    buffer.raw().writeBoolean(delivered);
    buffer.writeString(output);
    buffer.writeString(errorMessage);
    buffer.writeVarInt(exitCode);
  }
}
