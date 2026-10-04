package net.taskwolf.device.distribution.file.packet.incoming;

import net.taskwolf.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileInfoRequest extends PacketIncoming {
  private UUID infoId;
  private String deviceId;
  private DevicePlatform devicePlatform;
  private String filePath;
  private String fileName;

  public PacketIncomingFileInfoRequest() {
    super(0x28);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    infoId = buffer.readUUID();
    deviceId = buffer.readString();
    devicePlatform = DevicePlatform.valueOf(buffer.readString());
    filePath = buffer.readString();
    fileName = buffer.readString();
  }
}
