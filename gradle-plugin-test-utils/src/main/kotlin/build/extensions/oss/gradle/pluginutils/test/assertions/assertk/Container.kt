package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import org.gradle.api.NamedDomainObjectCollection


/**
 * Asserts that the [NamedDomainObjectCollection] contains an item with the given name. Transforms the
 * [Assert] so one can make further assertions on the item.
 *
 * @param name the item name
 * @return an [Assert] for further assertions on the item
 */
fun <T : Any> Assert<NamedDomainObjectCollection<T>>.containsItem(name: String) =
    transform { actual ->
        actual.findByName(name) ?: expected("to contain an item named \"$name\"")
    }


/**
 * Asserts that the [NamedDomainObjectCollection] contains no item with the given name.
 *
 * @param name the item name
 */
fun <T : Any> Assert<NamedDomainObjectCollection<T>>.doesNotContainItem(name: String) = given { actual ->
    val item = actual.findByName(name)
    if (item != null) {
        expected("to contain no item named \"$name\", but did contain: ${show(item)}")
    }
}
