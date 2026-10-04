package net.taskwolf.device;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class DeviceConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/device/device.json";

  public static DeviceConfiguration createAndLoad() throws Exception {
    var configuration = new DeviceConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String firebaseConfigurationName;
  private String firebaseProjectId;
  private int webSocketPort;

  private DeviceConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    firebaseConfigurationName = json.getString("firebaseConfigurationName");
    firebaseProjectId = json.getString("firebaseProjectId");
    webSocketPort = json.getInt("webSocketPort");
  }
}
