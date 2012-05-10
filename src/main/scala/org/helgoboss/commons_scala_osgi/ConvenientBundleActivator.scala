package org.helgoboss.commons_scala_osgi

/**
 * Extend from this class if you want to build a bundle activator in a very comfortable Scalaish way with everything included.
 * Extending from this class instead of mixing in the separate Osgi* traits you need has the big advantage that you don't need to
 * recompile your bundle if internal changes are made to the Osgi* traits. You just would have to replace the osgi-additions bundle.
 */
class ConvenientBundleActivator extends OsgiContext
    with OsgiBundleWatcher
    with OsgiConfigurationWatcher
    with OsgiConsumer
    with OsgiLogging
    with OsgiMetaTypeProvider
    with OsgiProvider
    with OsgiServiceWatcher