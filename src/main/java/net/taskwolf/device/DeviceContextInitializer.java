package net.taskwolf.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.command.CommandRequestRepository;
import net.taskwolf.device.file.FileDatabaseTable;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
public final class DeviceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CommandRequestRepository commandRequestRepository;
  private final FileDatabaseTable fileDatabaseTable;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;

  @Inject
  private DeviceContextInitializer(
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    NotificationFactory notificationFactory,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    CommandRequestRepository commandRequestRepository,
    FileDatabaseTable fileDatabaseTable,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable
  ) {
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.notificationFactory = notificationFactory;
    this.commandExecutionDatabaseTable = commandExecutionDatabaseTable;
    this.commandRequestRepository = commandRequestRepository;
    this.fileDatabaseTable = fileDatabaseTable;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("deviceDatabaseTable", deviceDatabaseTable);
    beanFactory.registerSingleton("userDeviceDatabaseTable", userDeviceDatabaseTable);
    beanFactory.registerSingleton("firebaseDeviceDatabaseTable", firebaseDeviceDatabaseTable);
    beanFactory.registerSingleton("notificationFactory", notificationFactory);
    beanFactory.registerSingleton("commandExecutionDatabaseTable", commandExecutionDatabaseTable);
    beanFactory.registerSingleton("commandRequestRepository", commandRequestRepository);
    beanFactory.registerSingleton("fileDatabaseTable", fileDatabaseTable);
    beanFactory.registerSingleton("fileStorageDatabaseTable", fileStorageDatabaseTable);
    beanFactory.registerSingleton("fileInfoDatabaseTable", fileInfoDatabaseTable);
  }
}
