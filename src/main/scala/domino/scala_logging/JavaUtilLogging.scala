package domino.scala_logging

import java.util.logging.{ Logger => JLogger }

/**
 * Convenience trait which you can mix into your class to have easy access to a logger based on Java Logging API.
 *
 * {{{
 *   import domino.scala_logging.JavaUtilLogging
 *
 *   class MyService extends JavaUtilLogging {
 *     log.debug("Hello World!")
 *   }
 * }}}
 */
trait JavaUtilLogging {
  /**
   * Logger delegating to Java Logging API.
   */
  protected val log = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))
}