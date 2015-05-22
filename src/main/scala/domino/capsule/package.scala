package domino

/**
 * Contains a basic API and default implementation for building, using and extending a capsule-based DSL.
 *
 * A capsule-based DSL is a generalization of the DSL used in the project "Domino". Here's an illustrative example
 * how a capsuled-based DSL might look like:
 *
 * {{{
 * // Somewhere in your code
 * whenTurnedOn {
 *   whenDevicePluggedIn {
 *     lightLED()
 *   }
 * }
 * }}}
 *
 * The documentation distinguishes between 3 types of API clients: End users, capsule providers and context providers.
 * The majority of developers will just come into contact with this API as end users.
 */
package object capsule {

}
