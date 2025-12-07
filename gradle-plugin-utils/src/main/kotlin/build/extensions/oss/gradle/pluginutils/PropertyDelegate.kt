package build.extensions.oss.gradle.pluginutils

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface PropertyDelegate<R, D> {

    operator fun provideDelegate(receiver: R, property: KProperty<*>): D
}


interface GenericPropertyDelegate<D> {

    operator fun <R> provideDelegate(receiver: R, property: KProperty<*>): D
}


interface ReadOnlyPropertyDelegate<R, T> : PropertyDelegate<R, ReadOnlyProperty<R, T>>


interface GenericReadOnlyPropertyDelegate<DR, T> : GenericPropertyDelegate<ReadOnlyProperty<DR, T>>
