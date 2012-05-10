package org.helgoboss.commons_scala_osgi

import org.osgi.framework.{ServiceReference, BundleContext}

object RichServiceReference {
    implicit def richServiceReferenceToServiceReference(richRef: RichServiceReference[_]) = richRef.ref
}

class RichServiceReference[S <: AnyRef: ClassManifest](val ref: ServiceReference, bundleContext: BundleContext) {
    def service = bundleContext.getService(ref).asInstanceOf[S]
}
