package org.helgoboss.commons_scala_osgi

import metatype._

import org.osgi.service.cm.ManagedService   
import org.osgi.service.metatype.{MetaTypeProvider => JMetaTypeProvider, MetaTypeInformation}
import java.util.Dictionary
import org.osgi.framework.Constants
        
trait OsgiMetaTypeProvider extends OsgiProvider with OsgiLogging {
    protected def provideMetaType(objectClassDefinition: ObjectClassDefinition) = {

        /* FIXME: Maybe because of a bug related to Apache Felix, we have to register the MetaTypeProvider. But maybe it's not a bug
         * and complies with the OSGi spec that we always have to register MetaTypeProvider + ManagedService as one object.
         */        
        new JMetaTypeProvider with ManagedService {
            val delegate = objectClassDefinition.toMetaTypeProvider.osgiCompliant
            
            def updated(config: Dictionary[_, _]) {
                /* Don't react. Just for satisfying Felix. */
            }
            
            def getLocales = delegate.getLocales
            def getObjectClassDefinition(id: String, localeString: String) = delegate.getObjectClassDefinition(id, localeString)
        }.providesService[JMetaTypeProvider, ManagedService](Constants.SERVICE_PID -> objectClassDefinition.id)   
    }
}