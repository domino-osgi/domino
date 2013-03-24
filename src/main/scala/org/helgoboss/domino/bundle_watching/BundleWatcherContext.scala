package org.helgoboss.domino.bundle_watching

import org.osgi.util.tracker.BundleTracker
import org.osgi.framework.Bundle

/**
 * Contains details about the current bundle event. For now, this just contains the underlying bundle tracker
 * but it might be expanded in future.
 *
 * @param tracker Underlying bundle tracker
 */
case class BundleWatcherContext(tracker: BundleTracker[Bundle])