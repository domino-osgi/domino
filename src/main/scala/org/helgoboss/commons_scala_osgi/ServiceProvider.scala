package org.helgoboss.commons_scala_osgi

import org.osgi.framework.BundleContext
import org.helgoboss.module_support._

class SimpleServiceProvider(
    protected val moduleContext: ModuleContext,
    protected val bundleContext: BundleContext) extends ServiceProvider {

  def this(osgiContext: OsgiContext) = this(osgiContext, osgiContext.bundleContext)
}

trait ServiceProvider {
  protected def bundleContext: BundleContext
  protected def moduleContext: ModuleContext

  implicit def service2RichService(service: AnyRef) = new RichService(service, moduleContext, bundleContext)
}