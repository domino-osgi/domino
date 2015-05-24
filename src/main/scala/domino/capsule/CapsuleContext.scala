package domino.capsule

/**
 * Represents the tree structure which is formed by capsule scopes and their contained capsules.
 *
 * As end user, you usually don't deal with this interface.
 *
 * As capsule provider, you use this interface for implementing the convenience methods and providing new
 * capsule scopes.
 *
 * As context provider, you can implement this interface. However, you can also use the existing
 * implementation [[DynamicCapsuleContext]].
 */
trait CapsuleContext {
  /**
   * Starts the given capsule and adds it to the current capsule scope.
   *
   * @param capsule capsule
   */
  def addCapsule(capsule: Capsule)

  /**
   * Creates a new capsule scope on top of the active one and executes the given function in it. So the function
   * sees the new capsule scope as the current one.
   *
   * @param f the function which might add capsules to the new scope
   * @return the new scope
   */
  def executeWithinNewCapsuleScope(f: => Unit): CapsuleScope
}
