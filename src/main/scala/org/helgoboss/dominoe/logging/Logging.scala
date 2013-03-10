package org.helgoboss.dominoe.logging

import org.helgoboss.scala_logging.{FallbackLogger, Logger, JavaUtilLoggingLogger}
import org.osgi.service.log.LogService
import java.util.logging.{Logger => JLogger}
import org.helgoboss.dominoe.service_consuming.ServiceConsuming

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
trait Logging {
  protected def serviceConsuming: ServiceConsuming

  private val fallbackLogger = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))

  val log: Logger = new FallbackLogger(primaryLogger, fallbackLogger)

  private def primaryLogger = serviceConsuming.optionalService[LogService] map { s => new OsgiLogger(s) }
}

