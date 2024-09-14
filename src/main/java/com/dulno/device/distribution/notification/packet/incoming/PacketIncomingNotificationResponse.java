package com.dulno.device.distribution.notification.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingNotificationResponse extends PacketIncoming {
  private UUID notificationId;
  private boolean delivered;

  public PacketIncomingNotificationResponse() {
    super(0x23);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    notificationId = buffer.readUUID();
    delivered = buffer.raw().readBoolean();
  }
}

