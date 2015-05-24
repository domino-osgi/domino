/**
 * The MIT License
 *
 * Copyright (c) 2011 Benjamin Klum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package domino.scala_logging

/**
 * Provides a common interface for logging in Scala.
 *
 * Makes use of Scala's Call-by-Name parameters so you don't have to check whether the desired log level is enabled
 * or not.
 *
 * Implementations should make sure that the arguments are evaluated only if the corresponding log level
 * is enabled. Additionatlly, they should handle the two-parameter methods like the single-parameter methods
 * if `null` is passed as the exception parameter.
 */
trait Logger {
  /**
   * Logs the given message on debug level.
   */
  def debug(message: => AnyRef)

  /**
   * Logs the given message and exception on debug level.
   */
  def debug(message: => AnyRef, exception: => Throwable)

  /**
   * Logs the given message on info level.
   */
  def info(message: => AnyRef)

  /**
   * Logs the given message and exception on info level.
   */
  def info(message: => AnyRef, exception: => Throwable)

  /**
   * Logs the given message on warn level.
   */
  def warn(message: => AnyRef)

  /**
   * Logs the given message and exception on warn level.
   */
  def warn(message: => AnyRef, exception: => Throwable)

  /**
   * Logs the given message on error level.
   */
  def error(message: => AnyRef)

  /**
   * Logs the given message and exception on error level.
   */
  def error(message: => AnyRef, exception: => Throwable)

  /**
   * Logs the given message on trace level.
   */
  def trace(message: => AnyRef)

  /**
   * Logs the given message and exception on trace level.
   */
  def trace(message: => AnyRef, exception: => Throwable)
}