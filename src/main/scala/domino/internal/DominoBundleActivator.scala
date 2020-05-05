package domino.internal

import domino.DominoActivator
import domino.service_watching.monitor.{ServiceWatchingMonitor, ServiceWatchingMonitorImpl}

class DominoBundleActivator extends DominoActivator {
  whenBundleActive {
    val watchMon = new ServiceWatchingMonitorImpl();
    watchMon.providesService[ServiceWatchingMonitor]
    onStop {
      watchMon.close()
    }
  }
}
