package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingNotificationResponse extends PacketOutgoing {
  private final UUID notificationId;
  private final boolean delivered;

  public PacketOutgoingNotificationResponse(
    UUID notificationId, boolean delivered
  ) {
    super(0x23);
    this.notificationId = notificationId;
    this.delivered = delivered;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(notificationId);
    buffer.raw().writeBoolean(delivered);
  }
}
