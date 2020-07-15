package io.harness.maintenance;

public class MaintenanceGuard implements AutoCloseable {
  private boolean old;
  public MaintenanceGuard(boolean maintenance) {
    old = MaintenanceController.getMaintenanceFlag();
    MaintenanceController.forceMaintenance(maintenance);
  }

  @Override
  public void close() {
    MaintenanceController.forceMaintenance(old);
  }
}
