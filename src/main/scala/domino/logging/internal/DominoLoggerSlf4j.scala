package domino.logging.internal

/**
 * Wrapper around Slf4j Logger.
 *
 * It's loading will fail, if no Slf4j API can be found on the classpath!
 */
class DominoLoggerSlf4j(loggedClass: String) extends DominoLogger {
  private[this] val underlying = org.slf4j.LoggerFactory.getLogger(loggedClass)

  override def isErrorEnabled: Boolean = underlying.isErrorEnabled()
  override def isWarnEnabled: Boolean = underlying.isWarnEnabled()
  override def isInfoEnabled: Boolean = underlying.isInfoEnabled()
  override def isDebugEnabled: Boolean = underlying.isDebugEnabled()
  override def isTraceEnabled: Boolean = underlying.isTraceEnabled()

  override def error(msg: => String): Unit = if (underlying.isErrorEnabled) underlying.error(msg)
  override def warn(msg: => String): Unit = if (underlying.isWarnEnabled) underlying.warn(msg)
  override def info(msg: => String): Unit = if (underlying.isInfoEnabled) underlying.info(msg)
  override def debug(msg: => String): Unit = if (underlying.isDebugEnabled) underlying.debug(msg)
  override def trace(msg: => String): Unit = if (underlying.isTraceEnabled) underlying.trace(msg)

  override def error(throwable: Throwable)(msg: => String): Unit = if (underlying.isErrorEnabled) underlying.error(msg, throwable)
  override def warn(throwable: Throwable)(msg: => String): Unit = if (underlying.isWarnEnabled) underlying.warn(msg, throwable)
  override def info(throwable: Throwable)(msg: => String): Unit = if (underlying.isInfoEnabled) underlying.info(msg, throwable)
  override def debug(throwable: Throwable)(msg: => String): Unit = if (underlying.isDebugEnabled) underlying.debug(msg, throwable)
  override def trace(throwable: Throwable)(msg: => String): Unit = if (underlying.isTraceEnabled) underlying.trace(msg, throwable)

  override def toString(): String = "Slf4j bridge wrapper for: " + loggedClass
}