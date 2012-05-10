package org.helgoboss.commons_scala_osgi.metatype

import org.osgi.service.metatype.{AttributeDefinition => JAttributeDefinition}

abstract class AttributeDefinition[T: ClassManifest] {
    def description: String
    def id: String
    def name: String
    def options: Option[Map[T, String]]
    def validate(value: T): ValidationResult
    def defaultValue: Option[_]
    
        
    
    protected trait OsgiCompliantAttributeDefinition extends JAttributeDefinition {
        def getDescription = description
        def getID = id
        def getName = name
        def getOptionLabels = options match {
            case Some(o) => o.keys.map(_.toString).toArray
            case None => null
        }
        def getOptionValues = options match {
            case Some(o) => o.values.toArray
            case None => null
        }
        def getType = {
            import JAttributeDefinition._
            classManifest[T].erasure match {
                case x if x == classOf[Boolean] => BOOLEAN
                case x if x == classOf[Byte] => BYTE
                case x if x == classOf[Char] => CHARACTER
                case x if x == classOf[Double] => DOUBLE
                case x if x == classOf[Float] => FLOAT
                case x if x == classOf[Int] => INTEGER
                case x if x == classOf[Long] => LONG
                case x if x == classOf[Short] => SHORT
                case x if x == classOf[String] => STRING
            }
        }
        
        def validate(value: String) = {
            import ValidationResult._
            try {
                val res = classManifest[T].erasure match {
                    case x if x == classOf[Boolean] => value.toBoolean
                    case x if x == classOf[Byte] => value.toByte
                    case x if x == classOf[Char] => if (value.isEmpty) throw new NumberFormatException else value(0)
                    case x if x == classOf[Double] => value.toDouble
                    case x if x == classOf[Float] => value.toFloat
                    case x if x == classOf[Int] => value.toInt
                    case x if x == classOf[Long] => value.toLong
                    case x if x == classOf[Short] => value.toShort
                    case x if x == classOf[String] => value
                }
                val t = res.asInstanceOf[T]
                
                AttributeDefinition.this.validate(t) match {
                    case Valid => ""
                    case Invalid(reason) => reason
                    case NotValidated => null
                }
            } catch {
                case x: NumberFormatException => "Incorrect type"
            }
        }
    }
    
    def osgiCompliant: JAttributeDefinition
}