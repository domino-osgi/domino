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

  override def error(msg: => String, throwable: Throwable): Unit = log(Level.SEVERE, msg, throwable)
  override def warn(msg: => String, throwable: Throwable): Unit = log(Level.WARNING, msg, throwable)
  override def info(msg: => String, throwable: Throwable): Unit = log(Level.INFO, msg, throwable)
  override def debug(msg: => String, throwable: Throwable): Unit = log(Level.FINE, msg, throwable)
  override def trace(msg: => String, throwable: Throwable): Unit =log(Level.FINER, msg, throwable)
  
  protected def log(level: Level, msg: String, cause: Throwable): Unit = 
   if(underlying.isLoggable(level)) underlying.log(level, msg, cause) 
  
  override def toString(): String = "JUL bridge wrapper for: " + loggedClass

}