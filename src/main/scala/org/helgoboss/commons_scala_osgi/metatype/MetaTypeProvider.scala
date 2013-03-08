package org.helgoboss.commons_scala_osgi.metatype

import java.util.Locale
import org.osgi.service.metatype.{ MetaTypeProvider => JMetaTypeProvider, ObjectClassDefinition => JObjectClassDefinition }

trait MetaTypeProvider {
  def locales: List[Locale]
  def getObjectClassDefinition(id: String, locale: Option[Locale]): ObjectClassDefinition

  lazy val osgiCompliant = new JMetaTypeProvider {
    lazy val LangCountryVariantRegex = "(.*)_(.*)_(.*)".r
    lazy val LangCountryRegex = "(.*)_(.*)".r
    lazy val LangRegex = "(.*)".r

    def getLocales = locales.map(_.toString).toArray
    def getObjectClassDefinition(id: String, localeString: String) = {
      val locale = if (localeString == null) None else localeString match {
        case LangCountryVariantRegex(lang, country, variant) => Some(new Locale(lang, country, variant))
        case LangCountryRegex(lang, country) => Some(new Locale(lang, country))
        case LangRegex(lang) => Some(new Locale(lang))
        case _ => sys.error("Not a valid locale string")
      }

      MetaTypeProvider.this.getObjectClassDefinition(id, locale).osgiCompliant
    }
  }
}