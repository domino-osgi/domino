package org.helgoboss.dominoe.service_providing

import org.osgi.framework.BundleContext
import org.helgoboss.capsule.CapsuleContext
import org.helgoboss.dominoe.OsgiContext

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
trait ServiceProviding {
  protected def bundleContext: BundleContext
  protected def capsuleContext: CapsuleContext

  implicit def serviceToProvidableService(service: AnyRef) = new ProvidableService(service, capsuleContext, bundleContext)
}

