package domino.capsule

import scala.util.DynamicVariable
import collection.mutable


/**
 * A [[CapsuleContext]] implementation based on [[scala.util.DynamicVariable]] and
 * [[DefaultCapsuleScope]].
 *
 * As a context provider, you might want to mix this trait into your class to provide the framework for a capsule-based
 * DSL. See class [[domino.OsgiContext]] in the project "Domino" for an example.
 */
trait DynamicCapsuleContext extends CapsuleContext {
  /**
   * A Set representing the current scope.
   */
  private val dynamicCapsuleSet = new DynamicVariable[Option[mutable.Set[Capsule]]](None)

  def addCapsule(capsule: Capsule) {
    // Start the capsule immediately
    capsule.start()

    // Add capsule to the current set if there is one
    dynamicCapsuleSet.value foreach { _ += capsule }
  }

  def executeWithinNewCapsuleScope(f: => Unit): CapsuleScope = {
    // Create the new set of capsules
    val newCapsuleSet = new mutable.HashSet[Capsule]

    // Execute the function in the new set
    dynamicCapsuleSet.withValue(Some(newCapsuleSet)) {
      f
    }

    // Returns the set wrapped in the scope interface
    new DefaultCapsuleScope(newCapsuleSet)
  }
}
