package domino.logging.internal

import java.util.logging.Logger
import java.util.logging.Level

/**
 * Logger wrapper around Java Util Logging.
 */
class DominoLoggerJul(loggedClass: String) extends DominoLogger {
  private[this] val underlying = Logger.getLogger(loggedClass)

  override def isErrorEnabled: Boolean = underlying.isLoggable(Level.SEVERE)
  override def isWarnEnabled: Boolean = underlying.isLoggable(Level.WARNING)
  override def isInfoEnabled: Boolean = underlying.isLoggable(Level.INFO)
  override def isDebugEnabled: Boolean = underlying.isLoggable(Level.FINE)
  override def isTraceEnabled: Boolean = underlying.isLoggable(Level.FINER)

  override def error(msg: => String): Unit = log(Level.SEVERE, msg)
  override def warn(msg: => String): Unit = log(Level.WARNING, msg)
  override def info(msg: => String): Unit = log(Level.INFO, msg)
  override def debug(msg: => String): Unit = log(Level.FINE, msg)
  override def trace(msg: => String): Unit = log(Level.FINER, msg)

  override def error(e: Throwable)(msg: => String): Unit = log(Level.SEVERE, msg, e)
  override def warn(e: Throwable)(msg: => String): Unit = log(Level.WARNING, msg, e)
  override def info(e: Throwable)(msg: => String): Unit = log(Level.INFO, msg, e)
  override def debug(e: Throwable)(msg: => String): Unit = log(Level.FINE, msg, e)
  override def trace(e: Throwable)(msg: => String): Unit = log(Level.FINER, msg, e)

  @inline
  private[this] def log(level: Level, msg: => String): Unit =
    if (underlying.isLoggable(level)) underlying.log(level, msg)

  @inline
  private[this] def log(level: Level, msg: => String, e: Throwable): Unit =
    if (underlying.isLoggable(level)) underlying.log(level, msg, e)

  override def toString(): String = "JUL bridge wrapper for: " + loggedClass

}