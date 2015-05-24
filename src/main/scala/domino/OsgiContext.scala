package domino

import org.osgi.framework.BundleContext
import domino.capsule._

/**
 * Provides the basis for the Domino DSL by binding the bundle lifecycle to a capsule scope.
 *
 * @groupname Basics Basics
 * @groupdesc Basics Basic methods
 * @see [[domino.capsule]]
 */
trait OsgiContext extends DynamicCapsuleContext with EmptyBundleActivator {
  /**
   * Contains the bundle context as long as the bundle is active.
   */
  private var _bundleContext: BundleContext = _

  /**
   * Returns the bundle context as long as the bundle is active.
   */
  def bundleContext = _bundleContext

  /**
   * Contains the handler that `whenBundleActive` has been called with.
   */
  private var bundleActiveHandler: Option[() => Unit] = None

  /**
   * Contains the capsule scope which was opened when the bundle was started.
   */
  private var bundleActiveCapsuleScope: Option[CapsuleScope] = None

  /**
   * Defines a handler `f` to be executed when the bundle becomes active. `f` is executed as soon as the bundle
   * activator's `start` method is called. This should be called in the constructor of your activator.
   * 
   * In `f`, you have the opportunity to add so called capsules, which have their own `start` and `stop` methods
   * (a kind of mini bundles). Their `stop` methods will be invoked as soon as the bundle activator's `stop` method
   * is called. So you have the big chance here to encapsulate start and stop logic at one place, making the bundle
   * activator less error-prone, better readable and easier to write.
   *
   * @group Basics
   * @param f Handler
   */
  def whenBundleActive(f: => Unit) {
    bundleActiveHandler = Some(f _)
  }

  abstract override def start(context: BundleContext) {
    // Integrate into the stacked traits
    super.start(context)

    // Make bundle context available in this class
    _bundleContext = context

    // Execute the handler if one was defined
    bundleActiveHandler foreach { f =>
      // Executes f. All capsules added in f are added to a new capsule scope which is returned afterwards.
      bundleActiveCapsuleScope = Some(executeWithinNewCapsuleScope(f()))
    }
  }

  abstract override def stop(context: BundleContext) {
    // Stop and release all the capsules in the scope
    bundleActiveCapsuleScope foreach { mc =>
      mc.stop()
      bundleActiveCapsuleScope = None
    }

    // Release bundle context
    _bundleContext = null

    // Integrate into the stacked traits
    super.stop(context)
  }
}