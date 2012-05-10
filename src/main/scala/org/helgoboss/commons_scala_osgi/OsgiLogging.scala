package org.helgoboss.commons_scala_osgi

import org.osgi.service.log.LogService
import org.helgoboss.commons_scala.Logger

trait OsgiLogging extends OsgiContext {
    protected val log = new Logger {
        def debug(message: => AnyRef) {
            log(LogService.LOG_DEBUG, message)
        }
        
        def debug(message: => AnyRef, exception: => Throwable) {
            log(LogService.LOG_DEBUG, message, exception)
        }
        
        def info(message: => AnyRef) {
            log(LogService.LOG_INFO, message)
        }
        
        def info(message: => AnyRef, exception: => Throwable) {
            log(LogService.LOG_INFO, message, exception)
        }
        
        def warn(message: => AnyRef) {
            log(LogService.LOG_WARNING, message)
        }
        
        def warn(message: => AnyRef, exception: => Throwable) {
            log(LogService.LOG_WARNING, message, exception)
        }
        
        def error(message: => AnyRef) {
            log(LogService.LOG_ERROR, message)
        }
        
        def error(message: => AnyRef, exception: => Throwable) {
            log(LogService.LOG_ERROR, message, exception)
        }
        
        def trace(message: => AnyRef) {
            log(LogService.LOG_DEBUG, message)
        }
        
        def trace(message: => AnyRef, exception: => Throwable) {
            log(LogService.LOG_DEBUG, message, exception)
        }
        
        private def log(logLevel: Int, message: AnyRef, throwable: Throwable = null) {
            val ref = bundleContext.getServiceReference(classOf[LogService].getName)
            if (ref != null) {
                val logService = bundleContext.getService(ref).asInstanceOf[LogService]
                if (logService != null) {
                    logService.log(logLevel, message.toString, throwable);
                }
            }
        }   
    }
}