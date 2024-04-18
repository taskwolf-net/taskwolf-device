package net.taskwolf.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.command.CommandRequestRepository;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.storage.FileStorageRepository;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
public final class DeviceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DistributionClientRegistry clientRegistry;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CommandRequestRepository commandRequestRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileStorageRepository fileStorageRepository;

  @Inject
  private DeviceContextInitializer(
    DistributionClientRegistry clientRegistry,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    NotificationFactory notificationFactory,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    CommandRequestRepository commandRequestRepository,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable,
    FileStorageRepository fileStorageRepository
  ) {
    this.clientRegistry = clientRegistry;
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.notificationFactory = notificationFactory;
    this.commandExecutionDatabaseTable = commandExecutionDatabaseTable;
    this.commandRequestRepository = commandRequestRepository;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileStorageRepository = fileStorageRepository;
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("clientRegistry", clientRegistry);
    beanFactory.registerSingleton("deviceDatabaseTable", deviceDatabaseTable);
    beanFactory.registerSingleton("userDeviceDatabaseTable", userDeviceDatabaseTable);
    beanFactory.registerSingleton("firebaseDeviceDatabaseTable", firebaseDeviceDatabaseTable);
    beanFactory.registerSingleton("notificationFactory", notificationFactory);
    beanFactory.registerSingleton("commandExecutionDatabaseTable", commandExecutionDatabaseTable);
    beanFactory.registerSingleton("commandRequestRepository", commandRequestRepository);
    beanFactory.registerSingleton("fileStorageDatabaseTable", fileStorageDatabaseTable);
    beanFactory.registerSingleton("fileInfoDatabaseTable", fileInfoDatabaseTable);
    beanFactory.registerSingleton("fileStorageRepository", fileStorageRepository);
  }
}
