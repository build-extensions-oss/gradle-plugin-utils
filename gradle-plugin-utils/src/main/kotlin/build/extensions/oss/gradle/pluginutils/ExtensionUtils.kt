package build.extensions.oss.gradle.pluginutils

import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionAware


@Deprecated(
    message = "Deprecated - disallow late runtime cast",
    replaceWith = ReplaceWith("extension(name, T::class.java)")
)
fun <T : Any> Any.extension(name: String): T? {
    @Suppress("UNCHECKED_CAST")
    return extension(name, Any::class.java) as T?
}

/**
 * Gets the extension of the given name if it exists.
 *
 * Will return `null` if the receiver is not [ExtensionAware].
 *
 * @receiver the object containing extensions
 * @param name the extension name
 * @return the extension, or `null` if it does not exist
 */
fun <T : Any> Any.extension(name: String, clazz: Class<T>): T? {
    val resultCandidate = (this as? ExtensionAware)?.extensions?.findByName(name)

    return clazz.cast(resultCandidate)
}


@Deprecated(
    message = "Deprecated - disallow late runtime cast",
    replaceWith = ReplaceWith("requiredExtension(name, T::class.java)")
)
fun <T : Any> Any.requiredExtension(name: String): T {
    @Suppress("UNCHECKED_CAST")
    return requiredExtension(name, Any::class.java) as T
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
 * Gets the extension of the given type if it exists.
 *
 * Will return `null` if the receiver is not [ExtensionAware].
 *
 * @receiver the object containing extensions
 * @param <T> the extension type
 * @return the extension, or `null` if it does not exist
 */
inline fun <reified T : Any> Any.extension(): T? =
    (this as? ExtensionAware)?.extensions?.findByType(typeOf<T>())


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


/**
 * Gets the convention plugin object of the given type, throwing an exception if it does not exist.
 *
 * @receiver the object containing conventions
 * @param <T> the convention type
 * @return the convention plugin object
 * @throws ClassCastException if the receiver object does not support conventions
 * @throws IllegalStateException if the convention plugin does not exist
 */
@Deprecated("prefer extension objects over conventions")
inline fun <reified T : Any> Any.requiredConventionPlugin(): T =
    ((this as ExtensionAware).extensions as Convention).getPlugin(T::class.java)
