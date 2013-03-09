package org.helgoboss.dominoe.service_watching

import org.osgi.util.tracker.ServiceTracker
import org.helgoboss.dominoe.RichServiceReference

case class ServiceWatcherContext[S <: AnyRef](tracker: ServiceTracker, reference: RichServiceReference[S])