package domino.logging.internal

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Experimental logger facade, meant for internal user in Domino OSGi.
 */
trait DominoLogger extends Serializable {
  def isErrorEnabled: Boolean
  def isWarnEnabled: Boolean
  def isInfoEnabled: Boolean
  def isDebugEnabled: Boolean
  def isTraceEnabled: Boolean

  def error(msg: => String, throwable: Throwable = null): Unit
  def warn(msg: => String, throwable: Throwable = null): Unit
  def info(msg: => String, throwable: Throwable = null): Unit
  def debug(msg: => String, throwable: Throwable = null): Unit
  def trace(msg: => String, throwable: Throwable = null): Unit
}

object DominoLogger {

  private[this] var cachedLoggerFactory: Option[String => DominoLogger] = None

  private[this] lazy val noOpLogger = new DominoLoggerNoop()
  private[this] lazy val noOpLoggerFactory: String => DominoLogger = _ => noOpLogger

  private[this] lazy val julLoggerFactory: String => DominoLogger = name => new DominoLoggerJul(name)

  /**
   * Experimental logger facade, meant for internal use in Domino OSGi.
   * It tries to load optional Slf4j and delegates logging to it.
   * If Slf4j is not available, it falls back to log nothing.
   * TODO: If Slf4j is not available, it falls back to java logging.
   * TODO: Also try to log to OSGi LogService, e.g. by injecting it into the facade.
   * TODO: Collect exerience with this API and possibly make it public later.
   */
  def apply[T: ClassTag]: DominoLogger = cachedLoggerFactory match {

    // user cached instance
    case Some(loggerFactory) => loggerFactory(classTag[T].runtimeClass.getName)

    case None =>
      // try to load SLF4J
      def delegatedLoadingOfLoggerFactory: String => DominoLogger = {
        // trigger loading of class, risking a NoClassDefFounError
        org.slf4j.LoggerFactory.getILoggerFactory()

        // if we are here, loading the LoggerFactory was successful
        loggedClass => new DominoLoggerSlf4j(loggedClass)
      }

      try {
        val loggerFactory = delegatedLoadingOfLoggerFactory
        cachedLoggerFactory = Some(loggerFactory)
        loggerFactory(classTag[T].runtimeClass.getName)
      } catch {
        case e: NoClassDefFoundError =>
          cachedLoggerFactory = Some(julLoggerFactory)
          julLoggerFactory(classTag[T].runtimeClass.getName)
      }
  }
}


