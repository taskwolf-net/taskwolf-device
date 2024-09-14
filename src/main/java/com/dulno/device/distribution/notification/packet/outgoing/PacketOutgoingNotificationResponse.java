package com.dulno.device.distribution.notification.packet.outgoing;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

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
