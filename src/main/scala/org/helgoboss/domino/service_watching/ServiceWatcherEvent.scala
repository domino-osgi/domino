package org.helgoboss.domino.service_watching


/**
 * Super class for service watcher events. The possible events are defined in the companion object.
 *
 * @param service Service affected by the state transition
 * @param context Additional event data
 */
abstract sealed class ServiceWatcherEvent[S <: AnyRef](service: S, context: ServiceWatcherContext[S])

/**
 * Contains the possible service watcher events.
 */
object ServiceWatcherEvent {

  /**
   * A service is being added to the ServiceTracker.
   */
  case class AddingService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)

  /**
   * A service tracked by the ServiceTracker has been modified.
   */
  case class ModifiedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)

  /**
   * A service tracked by the ServiceTracker has been removed.
   */
  case class RemovedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
}
