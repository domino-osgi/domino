package domino.logging.internal

class DominoLoggerNoop extends DominoLogger {
  def isErrorEnabled: Boolean = false
  def isWarnEnabled: Boolean = false
  def isInfoEnabled: Boolean = false
  def isDebugEnabled: Boolean = false
  def isTraceEnabled: Boolean = false
  override def error(msg: => String): Unit = {}
  override def warn(msg: => String): Unit = {}
  override def info(msg: => String): Unit = {}
  override def debug(msg: => String): Unit = {}
  override def trace(msg: => String): Unit = {}
  override def error(throwable: Throwable)(msg: => String): Unit = {}
  override def warn(throwable: Throwable)(msg: => String): Unit = {}
  override def info(throwable: Throwable)(msg: => String): Unit = {}
  override def debug(throwable: Throwable)(msg: => String): Unit = {}
  override def trace(throwable: Throwable)(msg: => String): Unit = {}
  override def toString(): String = "Noop Logger"
}