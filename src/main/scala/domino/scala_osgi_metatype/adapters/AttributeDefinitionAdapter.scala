package domino.scala_osgi_metatype.adapters

import org.osgi.service.metatype.{AttributeDefinition => JAttributeDefinition}
import domino.scala_osgi_metatype.interfaces.{Password, ValidationResult, AttributeDefinition}

/**
 * Provides the given Scala attribute definition as an OSGi-compliant attribute definition.
 *
 * @constructor Creates an adapter for the given definition.
 * @param delegate Scala attribute definition
 */
abstract class AttributeDefinitionAdapter[T](delegate: AttributeDefinition[T]) extends JAttributeDefinition {
  def getDescription = delegate.description

  def getID = delegate.id

  def getName = delegate.name

  lazy val getOptionLabels = {
    if (delegate.options.isEmpty) {
      null
    } else {
      delegate.options.map(_._1).toArray
    }
  }

  lazy val getOptionValues = {
    if (delegate.options.isEmpty) {
      null
    } else {
      delegate.options.map(_._2.toString).toArray
    }
  }

  lazy val getType = {
    import JAttributeDefinition._

    delegate.valueType match {
      case x if x == classOf[Boolean] => BOOLEAN
      case x if x == classOf[Byte] => BYTE
      case x if x == classOf[Char] => CHARACTER
      case x if x == classOf[Double] => DOUBLE
      case x if x == classOf[Float] => FLOAT
      case x if x == classOf[Int] => INTEGER
      case x if x == classOf[Long] => LONG
      case x if x == classOf[Short] => SHORT
      case x if x == classOf[String] => STRING
      case x if x == classOf[Password] => PASSWORD
    }
  }

  def validate(value: String) = {
    import ValidationResult._

    try {
      // Convert from String to generic type
      val convertedValue = delegate.valueType match {
        case x if x == classOf[Boolean] => value.toBoolean
        case x if x == classOf[Byte] => value.toByte
        case x if x == classOf[Char] => if (value.isEmpty) throw new NumberFormatException else value(0)
        case x if x == classOf[Double] => value.toDouble
        case x if x == classOf[Float] => value.toFloat
        case x if x == classOf[Int] => value.toInt
        case x if x == classOf[Long] => value.toLong
        case x if x == classOf[Short] => value.toShort
        case x if x == classOf[Password] => Password(value)
        case x if x == classOf[String] => value
      }

      // Validate value with generic type
      delegate.validate(convertedValue.asInstanceOf[T]) match {
        case Valid => ""
        case Invalid(reason) => reason
        case NotValidated => null
      }
    } catch {
      case x: NumberFormatException =>
        // Conversion failed. Not valid. Return reason.
        "Incorrect type"
    }
  }
}