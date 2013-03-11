package org.helgoboss.dominoe.logging

import org.helgoboss.dominoe.service_consuming.ServiceConsuming

/**
 * A class version of the [[org.helgoboss.dominoe.logging.Logging]] trait. Use this if you don't want to use the trait.
 */
class SimpleLogging(protected val serviceConsuming: ServiceConsuming) extends Logging
