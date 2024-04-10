package net.taskwolf.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.command.CommandRequestRepository;
import net.taskwolf.device.notification.DeviceNotificationDatabaseTable;
import net.taskwolf.device.notification.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.notification.NotificationDatabaseTable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DeviceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final DeviceNotificationDatabaseTable deviceNotificationDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CommandRequestRepository commandRequestRepository;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("deviceDatabaseTable", deviceDatabaseTable);
    beanFactory.registerSingleton("userDeviceDatabaseTable", userDeviceDatabaseTable);
    beanFactory.registerSingleton("pushNotificationDatabaseTable", notificationDatabaseTable);
    beanFactory.registerSingleton("deviceNotificationDatabaseTable", deviceNotificationDatabaseTable);
    beanFactory.registerSingleton("firebaseDeviceDatabaseTable", firebaseDeviceDatabaseTable);
    beanFactory.registerSingleton("notificationFactory", notificationFactory);
    beanFactory.registerSingleton("commandExecutionDatabaseTable", commandExecutionDatabaseTable);
    beanFactory.registerSingleton("commandRequestRepository", commandRequestRepository);
  }
}
