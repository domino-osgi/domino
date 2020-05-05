package domino.configuration_watching

import domino.capsule.Capsule
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import domino.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import domino.scala_osgi_metatype.interfaces.MetaTypeProvider


/**
 * Contains some common methods for both the configuration and factory configuration capsules.
 *
 * @constructor Initializes the capsule.
 * @param metaTypeProvider Optional meta type provider
 */
abstract class AbstractConfigurationWatcherCapsule(
    metaTypeProvider: Option[MetaTypeProvider]) extends Capsule with JMetaTypeProvider {
  
  /**
   * Contains the adapter which translates the Scala OSGi metatype definition into a native OSGi metatype definition.
   */
  private[this] lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter.map(_.getObjectClassDefinition(id, locale)).orNull
  }

  def getLocales = metaTypeProviderAdapter.map(_.getLocales).orNull
}
