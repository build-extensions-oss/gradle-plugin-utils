package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert


/**
 * Casts the actual value to the given type.
 *
 * @param T the target type for the cast
 * @return a new [Assert] with the same actual that acts as type [T]
 */
fun <T> Assert<*>.cast(): Assert<T> = transform { actual ->
    @Suppress("UNCHECKED_CAST")
    actual as T
}
