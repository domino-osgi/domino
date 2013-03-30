package org.helgoboss.domino.configuration_watching

import org.helgoboss.capsule.Capsule
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider


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
  protected lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter map { _.getObjectClassDefinition(id, locale) } orNull
  }

  def getLocales = metaTypeProviderAdapter map { _.getLocales } orNull
}
