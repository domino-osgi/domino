package org.helgoboss.commons_scala_osgi

abstract sealed class ServiceWatcherEvent[S <: AnyRef](service: S, context: ServiceWatcherContext[S])
case class AddingService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
case class ModifiedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
case class RemovedService[S <: AnyRef](service: S, context: ServiceWatcherContext[S]) extends ServiceWatcherEvent[S](service, context)
