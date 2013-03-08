package org.helgoboss.commons_scala_osgi

import org.osgi.framework.Bundle

abstract sealed class BundleWatcherEvent(bundle: Bundle, context: BundleWatcherContext)
case class AddingBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
case class ModifiedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
case class RemovedBundle(bundle: Bundle, context: BundleWatcherContext) extends BundleWatcherEvent(bundle, context)
