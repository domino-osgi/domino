package org.helgoboss.dominoe

import org.osgi.framework.BundleContext
import org.helgoboss.capsule._

/**
 * Mix in this trait if you want to build a bundle activator using a flexible DSL. Much like this:
 * <code>
 * whenBundleActive {
 *     whenServicePresent[MyService](_.sayHello)
 * }
 * </code>
 * The method "whenBundleActive", which is intended to be called in the constructor of your class, lets you define code f
 * to be executed when the bundle becomes active. f is executed as soon as the bundle activator's start method
 * is called. In f, you have the opportunity to add so called capsules, which have their own start and stop method (a kind of mini
 * bundles). Their start methods will be invoked right after f has been executed. And most important: Their stop methods
 * will be invoked as soon as the bundle activator's stop method is called. So you have the big chance here to encapsulate
 * start and stop logic at one place, making the bundle activator less error-prone, better readable and easier to write.
 *
 * There are already some predefined traits providing methods which feel like DSL components and add capsules themselves.
 * Like for example the trait OsgiServiceWatcher. For instance, it provides the method "whenServicePresent" used in the example.
 */
trait OsgiContext extends DynamicCapsuleContext with EmptyBundleActivator {
  /* Property containing the bundle context as long as the bundle is active */
  var bundleContext: BundleContext = _

  private var bundleActiveHandler: Option[() => Unit] = None
  private var bundleActiveCapsuleContainer: Option[CapsuleContainer] = None

  def whenBundleActive(f: => Unit) {
    bundleActiveHandler = Some(f _)
  }

  abstract override def start(context: BundleContext) {
    super.start(context)

    /* Make bundle context property available in this class */
    bundleContext = context

    /* Execute the function defined by "whenBundleActive", if any */
    bundleActiveHandler foreach { f =>
      /* Executes f. All capsules added in f are added to a new capsule container which is returned afterwards. */
      bundleActiveCapsuleContainer = Some(executeWithinNewCapsuleContainer(f()))
    }
  }

  abstract override def stop(context: BundleContext) {
    /* Stop and release all the capsules */
    bundleActiveCapsuleContainer foreach { mc =>
      mc.stop()
      bundleActiveCapsuleContainer = None
    }

    /* Release bundle context */
    bundleContext = null

    super.stop(context)
  }
}