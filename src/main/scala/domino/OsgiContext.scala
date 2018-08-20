package domino

import org.osgi.framework.BundleContext
import domino.capsule._
import domino.logging.internal.DominoLogger
import scala.util.control.NonFatal

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
  private[this] var _bundleContext: Option[BundleContext] = None

  /**
   * Contains the handler that `whenBundleActive` has been called with.
   */
  private[this] var bundleActiveHandler: Option[() => Unit] = None

  /**
   * Contains the capsule scope which was opened when the bundle was started.
   */
  private[this] var bundleActiveCapsuleScope: Option[CapsuleScope] = None

  private[this] val log = DominoLogger[OsgiContext]

  /**
   * Returns the bundle context as long as the bundle is active.
   */
  def bundleContext: BundleContext = _bundleContext.orNull // TODO: better throw exception here?

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
    log.debug(s"Registering whenBundleActive for bundle in ${getClass()}")
    if (bundleActiveHandler.isDefined) {
      log.warn(s"Overriding already present whenBundleActive for bundle in ${getClass()}. The previous whenBundleActive will be ignored")
    }
    bundleActiveHandler = Some(f _)
  }

  abstract override def start(context: BundleContext) {
    // Integrate into the stacked traits
    super.start(context)

    if (_bundleContext.isDefined) {
      log.warn(s"A BundleContext is already defined. Was the bundle started before? Bundle: ${dumpBundle(context)}")
    }

    // Make bundle context available in this class
    _bundleContext = Option(context)

    // Execute the handler if one was defined
    bundleActiveHandler.foreach { f =>
      log.debug(s"Starting whenBundleActive of bundle: ${dumpBundle(context)}")
      // Executes f. All capsules added in f are added to a new capsule scope which is returned afterwards.
      try {
        bundleActiveCapsuleScope = Some(executeWithinNewCapsuleScope(f()))
      } catch {
        case NonFatal(e) =>
          log.debug(e)(s"Exception thrown while starting whenBundleActive of bundle: ${dumpBundle(context)}")
          throw e
      }
    }
  }

  private[this] def dumpBundle(context: BundleContext): String = {
    val bundle = context.getBundle()
    s"${bundle.getSymbolicName()}[${bundle.getBundleId()}]"
  }

  abstract override def stop(context: BundleContext) {
    // Stop and release all the capsules in the scope
    try {
      bundleActiveCapsuleScope.foreach { mc =>
        try {
          log.debug(s"Stopping whenBundleActive of bundle: ${dumpBundle(context)}")
          mc.stop()
        } catch {
          case NonFatal(e) =>
            log.debug(e)(s"Exception thrown while stopping whenBundleActive of bundle: ${dumpBundle(context)}")
            throw e
        } finally {
          bundleActiveCapsuleScope = None
        }
      }

    } finally {
      // Release bundle context
      _bundleContext = None

      // Integrate into the stacked traits
      super.stop(context)
    }
  }
}