package org.helgoboss.domino.logging

import org.helgoboss.domino.service_consuming.ServiceConsuming

/**
 * A class that mixes in the [[Logging]] trait. Use this if you want to use a class instead of a trait.
 */
class SimpleLogging(protected val serviceConsuming: ServiceConsuming) extends Logging
