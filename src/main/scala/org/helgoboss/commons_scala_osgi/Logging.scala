package org.helgoboss.commons_scala_osgi

import org.osgi.service.log.LogService
import org.helgoboss.commons_scala.{ Logger, JavaUtilLoggingLogger, FallbackLogger }
import java.util.logging.{ Logger => JLogger }

class SimpleLogging(protected val serviceConsumer: ServiceConsumer) extends Logging

trait Logging {
  protected def serviceConsumer: ServiceConsumer

  private val fallbackLogger = new JavaUtilLoggingLogger(JLogger.getLogger(getClass.getName))

  val log: Logger = new FallbackLogger(primaryLogger, fallbackLogger)

  private def primaryLogger = serviceConsumer.optionalService[LogService] map { s => new OsgiLogger(s) }
}