package domino.service_watching.monitor

trait ServiceWatchingMonitor {
  def registerWatcher(watcher: ServiceWatcher): Unit
  def unregisterWatcher(watcher: ServiceWatcher): Unit
}
