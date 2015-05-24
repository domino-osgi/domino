package domino.scala_logging

/**
 * Logger decorator which logs to a secondary logger if the primary logger is not available.
 *
 * @constructor Creates the fallback logger.
 * @param primaryLogger Call-by-Name parameter which returns the primary logger if available and is evaluated
 *                      at every call to `log`. If it returns `None`, the secondary logger is used.
 * @param secondaryLogger The logger which is used if the primary logger is not available
 */
class FallbackLogger(primaryLogger: => Option[Logger], secondaryLogger: Logger) extends Logger {
    def debug(message: => AnyRef) {
        log(_.debug(message))
    }
    
    def debug(message: => AnyRef, exception: => Throwable) {
        log(_.debug(message, exception))
    }
    
    def info(message: => AnyRef) {
        log(_.info(message))
    }
    
    def info(message: => AnyRef, exception: => Throwable) {
        log(_.info(message, exception))
    }
    
    def warn(message: => AnyRef) {
        log(_.warn(message))
    }
    
    def warn(message: => AnyRef, exception: => Throwable) {
        log(_.warn(message, exception))
    }
    
    def error(message: => AnyRef) {
        log(_.error(message))
    }
    
    def error(message: => AnyRef, exception: => Throwable) {
        log(_.error(message, exception))
    }
    
    def trace(message: => AnyRef) {
        log(_.trace(message))
    }
    
    def trace(message: => AnyRef, exception: => Throwable) {
        log(_.trace(message, exception))
    }

    private def log(method: Logger => Unit) {
        primaryLogger match {
            case Some(l) => 
                method(l)
                
            case None => 
                method(secondaryLogger)
        }
    }
}