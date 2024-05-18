package net.taskwolf.device.distribution.command.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
