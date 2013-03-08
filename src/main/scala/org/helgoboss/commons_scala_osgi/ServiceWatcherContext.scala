package org.helgoboss.commons_scala_osgi

import org.osgi.util.tracker.ServiceTracker

case class ServiceWatcherContext[S <: AnyRef](tracker: ServiceTracker, reference: RichServiceReference[S])