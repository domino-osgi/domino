package domino.capsule

/**
 * A capsule scope implementation based on a [[scala.collection.Traversable]].
 *
 * @constructor Creates a capsule scope containing the given capsules.
 * @param capsules capsules in the scope
 */
class DefaultCapsuleScope(capsules: Traversable[Capsule]) extends CapsuleScope {
  def stop(): Unit = {
    capsules.foreach { _.stop() }
  }
}
