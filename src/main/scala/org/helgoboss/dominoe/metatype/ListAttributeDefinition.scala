package org.helgoboss.dominoe.metatype

abstract class ListAttributeDefinition[T: ClassManifest] extends AttributeDefinition[T] {
  def sizeLimit: Option[Int]
  def defaultValue: Option[List[T]]

  lazy val osgiCompliant = new OsgiCompliantAttributeDefinition {
    def getCardinality = sizeLimit match {
      case Some(x) => x
      case None => Int.MaxValue
    }

    def getDefaultValue = defaultValue match {
      case Some(l) => l map { _.toString } toArray
      case None => null
    }
  }
}