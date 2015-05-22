package domino.scala_osgi_metatype.interfaces

/**
 * Provides access to metatypes. Interface modeled after [[org.osgi.service.metatype.MetaTypeProvider]].
 */
trait MetaTypeProvider {
  /**
   * Returns a list of available locales.
   */
  def locales: Traversable[String]

  /**
   *  Returns an object class definition for the specified id localized to the specified locale.
   */
  def getObjectClassDefinition(id: String, locale: Option[String]): ObjectClassDefinition
}

