package com.dulno.device.distribution.notification.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingNotificationRequest extends PacketIncoming {
  private UUID notificationId;
  private String deviceId;
  private String title;
  private String body;

  public PacketIncomingNotificationRequest() {
    super(0x22);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    notificationId = buffer.readUUID();
    deviceId = buffer.readString();
    title = buffer.readString();
    body = buffer.readString();
  }
}
