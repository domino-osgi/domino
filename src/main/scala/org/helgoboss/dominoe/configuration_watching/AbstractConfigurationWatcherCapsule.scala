package org.helgoboss.dominoe.configuration_watching

import org.helgoboss.capsule.Capsule
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider}
import org.helgoboss.scala_osgi_metatype.adapters.MetaTypeProviderAdapter
import org.helgoboss.scala_osgi_metatype.interfaces.MetaTypeProvider
import org.osgi.framework.ServiceRegistration
import org.osgi.service.cm.ManagedService


/**
 * Contains some common methods for bose the configuration and factory configuration capsules.
 */
abstract class AbstractConfigurationWatcherCapsule(
    metaTypeProvider: Option[MetaTypeProvider]) extends Capsule with JMetaTypeProvider {

  protected lazy val metaTypeProviderAdapter = metaTypeProvider map { new MetaTypeProviderAdapter(_) }

  def getObjectClassDefinition(id: String, locale: String) = {
    metaTypeProviderAdapter map { _.getObjectClassDefinition(id, locale) } orNull
  }

  def getLocales = metaTypeProviderAdapter map { _.getLocales } orNull
}
