package org.helgoboss.domino.logging

import org.osgi.service.log.LogService
import org.helgoboss.scala_logging.Logger

/**
 * Logger implementation using the OSGi LogService.
 *
 * @constructor Creates the logger based on the given log service.
 * @param logService OSGi log service
 */
class OsgiLogger(logService: LogService) extends Logger {
  import LogService._

  def debug(message: => AnyRef) {
    log(LOG_DEBUG, message, null)
  }

  def debug(message: => AnyRef, exception: => Throwable) {
    log(LOG_DEBUG, message, exception)
  }

  def info(message: => AnyRef) {
    log(LOG_INFO, message, null)
  }

  def info(message: => AnyRef, exception: => Throwable) {
    log(LOG_INFO, message, exception)
  }

  def warn(message: => AnyRef) {
    log(LOG_WARNING, message, null)
  }

  def warn(message: => AnyRef, exception: => Throwable) {
    log(LOG_WARNING, message, exception)
  }

  def error(message: => AnyRef) {
    log(LOG_ERROR, message, null)
  }

  def error(message: => AnyRef, exception: => Throwable) {
    log(LOG_ERROR, message, exception)
  }

  def trace(message: => AnyRef) {
    log(LOG_DEBUG, message, null)
  }

  def trace(message: => AnyRef, exception: => Throwable) {
    log(LOG_DEBUG, message, exception)
  }

  protected def log(level: Int, message: => AnyRef, exception: => Throwable) {
    logService.log(level, message.toString, exception)
  }
}