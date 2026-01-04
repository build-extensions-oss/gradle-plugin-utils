package build.extensions.oss.gradle.pluginutils

import org.gradle.api.plugins.ExtensionAware

/**
 * Gets the extension of the given name if it exists.
 *
 * Will return `null` if the receiver is not [ExtensionAware].
 *
 * @receiver the object containing extensions
 * @param name the extension name
 * @return the extension, or `null` if it does not exist
 */
inline fun <reified T : Any> Any.requiredExtension(name: String): T {
    @Suppress("UNCHECKED_CAST")
    return requiredExtension(name, T::class.java)
}

/**
 * Gets the extension of the given name, throwing an exception if it does not exist.
 *
 * @receiver the object containing extensions
 * @param name the extension name
 * @return the extension
 * @throws ClassCastException if the receiver object is not [ExtensionAware]
 * @throws org.gradle.api.UnknownDomainObjectException if the extension does not exist
 */
fun <T : Any> Any.requiredExtension(name: String, clazz: Class<T>): T {
    val returnCandidate = (this as ExtensionAware).extensions.getByName(name)

    return clazz.cast(returnCandidate)
}


/**
 * Gets the extension of the given type, throwing an exception if it does not exist.
 *
 * @receiver the object containing extensions
 * @param <T> the extension type
 * @return the extension
 * @throws ClassCastException if the receiver object is not [ExtensionAware]
 * @throws org.gradle.api.UnknownDomainObjectException if the extension does not exist
 */
inline fun <reified T : Any> Any.requiredExtension(): T =
    (this as ExtensionAware).extensions.getByType(typeOf<T>())

