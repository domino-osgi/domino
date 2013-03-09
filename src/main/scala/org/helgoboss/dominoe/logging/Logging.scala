package org.helgoboss.dominoe.logging

import org.helgoboss.dominoe.service_consuming.ServiceConsumer
import org.helgoboss.commons_scala.{FallbackLogger, Logger, JavaUtilLoggingLogger}
import org.osgi.service.log.LogService
import java.util.logging.{Logger => JLogger}

/**
 * Created with IntelliJ IDEA.
 * User: bkl
 * Date: 09.03.13
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
trait Logging {
  protected def serviceConsumer: ServiceConsumer

  private val fallbackLogger = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))

  val log: Logger = new FallbackLogger(primaryLogger, fallbackLogger)

  private def primaryLogger = serviceConsumer.optionalService[LogService] map { s => new OsgiLogger(s) }
}

class SimpleLogging(protected val serviceConsumer: ServiceConsumer) extends Logging