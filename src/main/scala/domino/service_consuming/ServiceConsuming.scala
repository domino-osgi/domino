package domino.service_consuming

import org.osgi.framework.{ServiceReference, BundleContext}
import domino.{DominoImplicits, DominoUtil}
import reflect.ClassTag
import reflect.classTag
import scala.reflect.runtime.universe._

/**
 * Provides convenient methods to consume OSGi services.
 *
 * @groupname GetServices Consume services
 * @groupdesc GetServices Methods for obtaining access to OSGi services
 * @groupname GetServiceReferences Consume service references
 * @groupdesc GetServiceReferences Methods for obtaining access to OSGi service references
 */
trait ServiceConsuming extends DominoImplicits {
  /** Dependency */
  protected def bundleContext: BundleContext

  /**
   * Executes the given handler with the highest-ranked service of the specified type. If it's not available,
   * it still executes it but with `None`. Doesn't take type parameters into account!
   *
   * When the handler returns, the service is released using [[org.osgi.framework.BundleContext#ungetService]].
   *
   * @group GetServices
   * @param f handler that uses the service
   * @tparam S service type
   * @tparam R function result type
   * @return handler result
   */
  def withService[S <: AnyRef: ClassTag, R](f: Option[S] => R): R = {
    serviceRef[S] match {
      case Some(ref) =>
        val service = bundleContext.getService(ref)
        try f(Some(service)) finally bundleContext.ungetService(ref)

      case None =>
        f(None)
    }
  }

  /**
   * Executes the given handler with the first available service of the specified class which satisfies the filter
   * if available. If it's not available, it still executes it but with `None`.
   *
   * When the handler returns, the service is released using [[org.osgi.framework.BundleContext#ungetService]].
   *
   * @group GetServices
   * @param filter filter expression
   * @param f handler that uses the service
   * @tparam S service type
   * @tparam R function result type
   * @return handler result
   */
  def withAdvancedService[S <: AnyRef: TypeTag: ClassTag, R](filter: String)(f: Option[S] => R): R = {
    serviceRef[S](filter) match {
      case Some(ref) =>
        val service = bundleContext.getService(ref)
        try f(Some(service)) finally bundleContext.ungetService(ref)

      case None =>
        f(None)
    }
  }

  /**
   * Returns the highest-ranked service of the specified type if available. The service is not explicitly released. 
   * It's assumed that the service will be used until the bundle stops. Doesn't take type parameters into account!
   *
   * @group GetServices
   * @tparam S service type
   * @return service if available
   */
  def service[S <: AnyRef: ClassTag]: Option[S] = {
    serviceRef[S] map { bundleContext.getService(_) }
  }

  /**
   * Like [[service]] but returns the reference so you can access meta information about that service.
   * An implicit conversion adds a `service` property to the reference, so you can simply use that to obtain the service.
   * Doesn't take type parameters into account!
   *
   * @group GetServiceReferences
   */
  def serviceRef[S <: AnyRef: ClassTag]: Option[ServiceReference[S]] = {
    val className = classTag[S].runtimeClass.getName
    val ref = bundleContext.getServiceReference(className)
    Option(ref) map { _.asInstanceOf[ServiceReference[S]] }
  }

  /**
   * Returns the first available service of the specified class which satisfies the filter if available. If the service
   * is not available, it returns `None`. The service is not explicitly released.
   *
   * @group GetServices
   * @param filter filter expression
   * @tparam S service type
   * @return service if available
   */
  def service[S <: AnyRef: TypeTag: ClassTag](filter: String): Option[S] = {
    serviceRef[S](filter) map { bundleContext.getService(_) }
  }

  /**
   * Like [[service]] with filter but returns the service reference.
   *
   * @group GetServiceReferences
   */
  def serviceRef[S <: AnyRef: TypeTag: ClassTag](filter: String): Option[ServiceReference[S]] = {
    val refs = serviceRefs[S](filter)
    refs.lift(0)
  }

  /**
   * Returns all services of the given type.
   *
   * @group GetServices
   * @tparam S service type
   */
  def services[S <: AnyRef: TypeTag: ClassTag]: Seq[S] = {
    services[S](null.asInstanceOf[String])
  }

  /**
   * Like [[services]] but returns service references.
   *
   * @group GetServiceReferences
   */
  def serviceRefs[S <: AnyRef: TypeTag: ClassTag]: Seq[ServiceReference[S]] = {
    serviceRefs[S](null.asInstanceOf[String])
  }

  /**
   * Returns all services of the specified type which satisfy the given filter.
   *
   * @group GetServices
   * @param filter filter expression
   * @tparam S service type
   * @return services
   */
  def services[S <: AnyRef: TypeTag: ClassTag](filter: String): Seq[S] = {
    serviceRefs[S](filter) map { bundleContext.getService(_) }
  }

  /**
   * Like [[services]] with filters but returns the references.
   *
   * @group GetServiceReferences
   */
  def serviceRefs[S <: AnyRef: TypeTag: ClassTag](filter: String): Seq[ServiceReference[S]] = {
    if (bundleContext == null) {
      Nil
    } else {
      // Build the filter (generic type filter + custom filter)
      val tpe = typeTag[S].tpe
      val completeFilter = DominoUtil.createGenericsAndCustomFilter(tpe, filter)

      // Get the list of references matching the filter
      val refs = bundleContext.getServiceReferences(DominoUtil.getFullTypeName(tpe), completeFilter.orNull)

      if (refs == null) {
        Nil
      } else {
        refs.toList.asInstanceOf[Seq[ServiceReference[S]]]
      }
    }
  }

}

