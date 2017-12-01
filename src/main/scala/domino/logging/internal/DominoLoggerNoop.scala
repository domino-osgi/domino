package domino.logging.internal

class DominoLoggerNoop extends DominoLogger {
  def isErrorEnabled: Boolean = false
  def isWarnEnabled: Boolean = false
  def isInfoEnabled: Boolean = false
  def isDebugEnabled: Boolean = false
  def isTraceEnabled: Boolean = false
  override def error(msg: => String, throwable: Throwable): Unit = {}
  override def warn(msg: => String, throwable: Throwable): Unit = {}
  override def info(msg: => String, throwable: Throwable): Unit = {}
  override def debug(msg: => String, throwable: Throwable): Unit = {}
  override def trace(msg: => String, throwable: Throwable): Unit = {}
  override def toString(): String = "Noop Logger"
}