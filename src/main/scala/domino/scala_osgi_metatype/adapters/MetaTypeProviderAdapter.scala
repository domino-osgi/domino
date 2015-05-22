package domino.scala_osgi_metatype.adapters

import org.osgi.service.metatype.{ MetaTypeProvider => JMetaTypeProvider }
import domino.scala_osgi_metatype.interfaces.MetaTypeProvider

/**
 * Provides the given Scala meta type provider as an OSGi-compliant meta type provider.
 *
 * This one should be registered along with a [[org.osgi.service.cm.ManagedService]] or
 * [[org.osgi.service.cm.ManagedServiceFactory]] to make the metatype data available to
 * the OSGi framework.
 *
 * @constructor Creates an adapter for the given provider.
 * @param delegate Scala meta type provider
 */
class MetaTypeProviderAdapter(delegate: MetaTypeProvider) extends JMetaTypeProvider {
  def getLocales = delegate.locales.toArray

  def getObjectClassDefinition(id: String, locale: String) = {
    val d = delegate.getObjectClassDefinition(id, Option(locale))
    new ObjectClassDefinitionAdapter(d)
  }
}
