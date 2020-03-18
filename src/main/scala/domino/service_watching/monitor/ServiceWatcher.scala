package domino.service_watching.monitor

trait ServiceWatcher {
  def target: String
  def isSatisfied: Boolean
}
