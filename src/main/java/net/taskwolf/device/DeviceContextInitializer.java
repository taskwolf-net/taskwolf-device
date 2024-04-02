package net.taskwolf.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DeviceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("deviceDatabaseTable", deviceDatabaseTable);
    beanFactory.registerSingleton("userDeviceDatabaseTable", userDeviceDatabaseTable);
  }
}
