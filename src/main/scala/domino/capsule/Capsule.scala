package domino.capsule

/**
 * A capsule represents something which is startable and stoppable. After being started and before being stopped, we say
 * the capsule is active. Otherwise, we say it's inactive.
 *
 * As end user, you usually don't come into contact with capsules directly, you rather add them using convenience methods.
 *
 * As capsule provider, you should implement this interface in order to provide logic which will be started and
 * stopped on certain events. You might also want to implement convenience methods which use
 * [[CapsuleContext]] to add the logic to the current capsule scope.
 */
trait Capsule {
  /**
   * Starts the capsule. After that, the capsule is active.
   */
  def start(): Unit

  /**
   * Stops the capsule. After that, the capsule is inactive.
   */
  def stop(): Unit
}
