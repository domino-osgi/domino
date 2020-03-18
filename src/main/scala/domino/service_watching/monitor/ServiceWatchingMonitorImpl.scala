package domino.service_watching.monitor

import java.util.{Timer, TimerTask}

import scala.util.Try

import domino.logging.internal.DominoLogger

class ServiceWatchingMonitorImpl(
    logInterval: Option[Int] = None
) extends ServiceWatchingMonitor {

  import ServiceWatchingMonitorImpl._

  private[this] val log = DominoLogger[ServiceWatchingMonitorImpl]

  private[this] var watchers: Set[ServiceWatcher] = Set()
  private[this] var timer: Option[Timer] = None

  protected def init(): Unit = {
    val timer = new Timer("ServiceWatchingMonitor-Log", true)
    val interval = logInterval
      .orElse(Try(System.getProperty(sysProperty_LogInterval).toInt).toOption)
      .getOrElse(defaultLogInterval)

    if (interval > 0) {
      log.debug(s"Starting logging service watch monitor with delay [${interval} ms]")
      val task = new TimerTask {
        override def run(): Unit = {
          def format(watchers: Iterable[ServiceWatcher]): String = {
            watchers.groupBy(_.target).mapValues("" + _.size).mkString(", ")
          }

          log.debug(s"Unsatisfied service watchers: ${format(watchers.filterNot(_.isSatisfied))}")
          log.trace(s"Satisfied service watchers: ${format(watchers.filter(_.isSatisfied))}")
        }
      }
      timer.schedule(task, interval, interval)
    } else {
      log.debug(s"Logging service watch monitor is disabled (interval=[${interval}])")
    }
  }

  init()

  def registerWatcher(watcher: ServiceWatcher): Unit = {
    watchers += watcher
  }

  def unregisterWatcher(watcher: ServiceWatcher): Unit = {
    watchers -= watcher
  }

  def close(): Unit = {
    log.debug("Stopping logging service watch monitor")
    this.timer.foreach { t =>
      t.cancel()
      this.timer = None
    }
    watchers = Set()
  }

}

object ServiceWatchingMonitorImpl {
  val sysProperty_LogInterval = "domino.service_watching.monitor.interval"
  val defaultLogInterval = 30000
}
