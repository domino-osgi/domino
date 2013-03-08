package org.helgoboss.dominoe

import org.osgi.util.tracker.ServiceTracker

case class ServiceWatcherContext[S <: AnyRef](tracker: ServiceTracker, reference: RichServiceReference[S])