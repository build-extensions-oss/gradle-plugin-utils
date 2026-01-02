package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.support.expected
import assertk.assertions.support.show
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File


/**
 * Asserts that the [Provider] has a value present, and transforms the assertion so one can
 * continue asserting on the provided value.
 */
fun <T : Any> Assert<Provider<out T>>.isPresent() = transform { actual ->
    actual.orNull ?: expected("${show(actual)} to have a value", actual = actual)
}


/**
 * Asserts that the value of the given boolean [Provider] is `true`.
 */
fun Assert<Provider<Boolean>>.isTrue() =
    isPresent().isTrue()


/**
 * Asserts that the value of the given boolean [Provider] is `false`.
 */
fun Assert<Provider<Boolean>>.isFalse() =
    isPresent().isFalse()


/**
 * Asserts that the provider has a value present which is equal to the given value.
 *
 * @param value the expected value
 */
fun <T : Any> Assert<Provider<T>>.hasValueEqualTo(value: T) =
    isPresent().isEqualTo(value)


/**
 * Asserts that the [RegularFile] provider has a value present, and allows a chained assertion
 * on the [File] object that describes the path to the file.
 */
fun Assert<Provider<RegularFile>>.fileValue(): Assert<File> =
    isPresent()
        .prop("file") { it.asFile }


/**
 * Asserts that the [Directory] provider has a value present, and allows a chained assertion
 * on the [File] object that describes the path to the directory.
 */
fun Assert<Provider<Directory>>.dirValue(): Assert<File> =
    isPresent()
        .prop("directory") { it.asFile }


/**
 * Asserts that a [Map] provider is present and contains the given key/value pair.
 */
fun <K : Any, V : Any> Assert<Provider<Map<K, V>>>.contains(key: K, value: V) =
    isPresent().contains(key, value)


/**
 * Asserts that the given [Iterable] provider contains an empty [Iterable].
 */
fun Assert<Provider<out Iterable<*>>>.isPresentAndEmpty() =
    isPresent().isEmpty()


/**
 * Asserts that the given [Map] provider contains an empty [Map].
 */
fun Assert<Provider<out Map<*, *>>>.isPresentAndEmptyMap() =
    isPresent().isEmpty()
