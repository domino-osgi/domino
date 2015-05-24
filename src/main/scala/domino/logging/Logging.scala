package domino.logging

import domino.scala_logging.{FallbackLogger, Logger, JavaUtilLoggingLogger}
import org.osgi.service.log.LogService
import java.util.logging.{Logger => JLogger}
import domino.service_consuming.ServiceConsuming

/**
 * Provides an OSGi logger with Java Logging API fallback.
 
 * @groupname LogMessages Log messages
 * @groupdesc LogMessages Functionality related to logging
 */
trait Logging {
  /** Dependency */
  protected def serviceConsuming: ServiceConsuming

  /** Fallback if OSGi logger is not available */
  protected lazy val fallbackLogger = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))

  /**
   * A logger for the current bundle. Uses the OSGi logging facility under the hood. If it is not available,
   * it uses Java Logging API.
   * 
   * @group LogMessages
   */
  val log: Logger = new FallbackLogger(primaryLogger, fallbackLogger)

  /**
   * The OSGi logger.
   *
   * @todo Always looked up dynamically. Can become a bottleneck.
   */
  protected def primaryLogger = serviceConsuming.service[LogService] map { s => new OsgiLogger(s) }
}

