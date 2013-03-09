package org.helgoboss.dominoe.service_watching

import org.helgoboss.capsule.Capsule
import org.osgi.util.tracker.ServiceTracker
import org.osgi.framework.{BundleContext, ServiceReference, Constants}
import org.helgoboss.dominoe.{DominoeUtil, RichServiceReference}

class ServiceWatcherCapsule[S <: AnyRef: ClassManifest](
    mf: ClassManifest[_],
    f: ServiceWatcherEvent[S] => Unit,
    bundleContext: BundleContext) extends Capsule {

  var tracker: ServiceTracker = _

  def start() {
    /* Construct filter containing generic type info if necessary */
    val objectClassFilterString = Some("(" + Constants.OBJECTCLASS + "=" + mf.erasure.getName + ")")
    val completeTypesExpressionFilterString = DominoeUtil.createCompleteTypeExpressionFilter(mf)

    val completeFilterString = DominoeUtil.linkFiltersWithAnd(
      objectClassFilterString,
      completeTypesExpressionFilterString)

    val completeFilter = bundleContext.createFilter(completeFilterString.get)

    /* Create tracker matching this filter */
    tracker = new ServiceTracker(bundleContext, completeFilter, null) {
      override def addingService(ref: ServiceReference) = {
        val service = context getService ref
        val watcherEvent = ServiceWatcherEvent.AddingService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
        f(watcherEvent)
        service
      }

      override def modifiedService(ref: ServiceReference, service: AnyRef) {
        val watcherEvent = ServiceWatcherEvent.ModifiedService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
        f(watcherEvent)
      }

      override def removedService(ref: ServiceReference, service: AnyRef) {
        val watcherEvent = ServiceWatcherEvent.RemovedService(service.asInstanceOf[S], ServiceWatcherContext(tracker, new RichServiceReference[S](ref, bundleContext)))
        f(watcherEvent)
        context ungetService ref
      }
    }
    tracker.open()
  }

  def stop() {
    tracker.close()
    tracker = null
  }
}