package org.helgoboss.dominoe.logging

import org.helgoboss.scala_logging.{FallbackLogger, Logger, JavaUtilLoggingLogger}
import org.osgi.service.log.LogService
import java.util.logging.{Logger => JLogger}
import org.helgoboss.dominoe.service_consuming.ServiceConsuming

/**
 * Provides an an OSGi logger with Java Util Logging fallback in the `log` value.
 */
trait Logging {
  /** Dependency */
  protected def serviceConsuming: ServiceConsuming

  /** Fallback if OSGi logger is not available */
  protected lazy val fallbackLogger = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))

  /**
   * A logger for the current bundle. Uses the OSGi logging facility under the hood. If it is not available,
   * it uses Java Util Logging.
   */
  val log: Logger = new FallbackLogger(primaryLogger, fallbackLogger)

  /**
   * The OSGi logger.
   *
   * TODO Is always looked up dynamically. That can become a bottleneck.
   */
  protected def primaryLogger = serviceConsuming.service[LogService] map { s => new OsgiLogger(s) }
}

