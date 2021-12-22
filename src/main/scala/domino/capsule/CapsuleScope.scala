package domino.capsule

/**
 * Represents a collection of capsules which shall all be stopped on the same event.
 *
 * As end user, you don't deal with this interface at all.
 *
 * As capsule provider, you might use this interface but you don't have to implement it. An object of this type is
 * returned from [[CapsuleContext.executeWithinNewCapsuleScope]] which is used to create a new
 * scope. You should then use this object later to stop all capsules in your new scope when the stopping event occurs.
 */
trait CapsuleScope {
  /**
   * Stops all capsules in this scope.
   */
  def stop(): Unit
}
