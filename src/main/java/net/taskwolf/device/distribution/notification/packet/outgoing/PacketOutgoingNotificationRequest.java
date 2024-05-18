package net.taskwolf.device.distribution.notification.packet.outgoing;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

import java.util.UUID;

public final class PacketOutgoingNotificationRequest extends PacketOutgoing {
  private final UUID notificationId;
  private final String deviceId;
  private final String title;
  private final String body;

  public PacketOutgoingNotificationRequest(
    UUID notificationId, String deviceId, String title, String body
  ) {
    super(0x22);
    this.notificationId = notificationId;
    this.deviceId = deviceId;
    this.title = title;
    this.body = body;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(notificationId);
    buffer.writeString(deviceId);
    buffer.writeString(title);
    buffer.writeString(body);
  }
}