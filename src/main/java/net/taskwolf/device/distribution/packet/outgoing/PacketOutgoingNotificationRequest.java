package net.taskwolf.device.distribution.packet.outgoing;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

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