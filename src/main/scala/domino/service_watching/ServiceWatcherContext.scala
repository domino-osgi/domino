package domino.service_watching

import org.osgi.util.tracker.ServiceTracker
import org.osgi.framework.ServiceReference

/**
 * Contains details about the current service event. Might be expanded in future.
 *
 * @param tracker Underlying service tracker
 * @param ref Service reference
 */
case class ServiceWatcherContext[S <: AnyRef](tracker: ServiceTracker[S, S], ref: ServiceReference[S])