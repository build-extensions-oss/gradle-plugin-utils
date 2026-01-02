package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import org.gradle.api.plugins.ExtensionAware


/**
 * Asserts that the object is [ExtensionAware] and has an extension with the given name.
 *
 * @param name the extension name
 * @return an [Assert] allowing further assertions on the extension
 */
fun Assert<Any>.hasExtensionNamed(name: String): Assert<Any> =
    transform("extension \"$name\"") { actual ->
        if (actual !is ExtensionAware) {
            expected("to be ExtensionAware")
        }
        actual.extensions.findByName(name)
            ?: expected("to have an extension named \"$name\"")
    }


/**
 * Asserts that the object is [ExtensionAware] and has an extension with the given type.
 *
 * @param E the extension type
 * @param name the optional extension name. If `null`, only the extension type is checked.
 * @return an [Assert] allowing further assertions on the extension
 */
inline fun <reified E : Any> Assert<Any>.hasExtension(name: String? = null): Assert<E> =
    transform("extension " + (name?.let { "\"$it\"" } ?: show(E::class))) { actual ->
        if (actual !is ExtensionAware) {
            expected("to be ExtensionAware")
        }
        val extensions = actual.extensions

        if (name != null) {
            val extension = extensions.findByName(name)
                ?: expected("to have an extension named \"$name\" of type ${show(E::class)}")
            (extension as? E)
                ?: expected(
                    "to have an extension named \"$name\" of type ${show(E::class)}, " +
                            "but actual type was: ${show(extension.javaClass)}"
                )

        } else {
            extensions.findByType(E::class.java)
                ?: expected("to have an extension of type ${show(E::class)}")
        }
    }
