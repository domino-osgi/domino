package domino.capsule

/**
 * Provides convenient `onStart` and `onStop` methods which the end user can use for ad-hoc adding
 * start and stop logic to the current scope.
 */
trait CapsuleConvenience {
  protected def capsuleContext: CapsuleContext

  /**
   * Adds the given start logic. The logic is executed immediately.
   *
   * @param f start logic
   */
  def onStart(f: => Unit): Unit = {
    // Create a capsule which just contains start logic
    val capsule = new Capsule {
      def start(): Unit = f
      def stop(): Unit = {}
    }

    // Add the capsule to the current scope
    capsuleContext.addCapsule(capsule)
  }

  /**
   * Adds the given stop logic. The given function will be executed when the current scope is stopped.
   *
   * @param f stop logic
   */
  def onStop(f: => Unit): Unit = {
    // Create a capsule which just contains stop logic
    val capsule = new Capsule {
      def start(): Unit = {}
      def stop(): Unit = f
    }

    // Add the capsule to the current scope
    capsuleContext.addCapsule(capsule)
  }
}
