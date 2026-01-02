package build.extensions.oss.gradle.pluginutils

import org.gradle.util.GradleVersion
import kotlin.reflect.KProperty


/**
 * Defines convenience accessors for common Gradle versions.
 */
@Suppress("unused")
object GradleVersions {

    val Version_4_0 by "4.0"
    val Version_4_1 by "4.1"
    val Version_4_2 by "4.2"
    val Version_4_3 by "4.3"
    val Version_4_4 by "4.4"
    val Version_4_5 by "4.5"
    val Version_4_6 by "4.6"
    val Version_4_7 by "4.7"
    val Version_4_8 by "4.8"
    val Version_4_9 by "4.9"
    val Version_4_10 by "4.10"

    val Version_5_0 by "5.0"
    val Version_5_1 by "5.1"
    val Version_5_2 by "5.2"
    val Version_5_3 by "5.3"
    val Version_5_4 by "5.4"
    val Version_5_5 by "5.5"
    val Version_5_6 by "5.6"

    val Version_6_0 by "6.0"
    val Version_6_1 by "6.1"
    val Version_6_2 by "6.2"
    val Version_6_3 by "6.3"
    val Version_6_4 by "6.4"
    val Version_6_5 by "6.5"
    val Version_6_6 by "6.6"
    val Version_6_7 by "6.7"
    val Version_6_8 by "6.8"
    val Version_6_9 by "6.9"

    val Version_7_0 by "7.0"
    val Version_7_1 by "7.1"
    val Version_7_2 by "7.2"
    val Version_7_3 by "7.3"
    val Version_7_4 by "7.4"
    val Version_7_5 by "7.5"
    val Version_7_6 by "7.6"

    val Version_8_1 by "8.1"
    val Version_8_2 by "8.2"
    val Version_8_3 by "8.3"

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun String.getValue(thisRef: Any?, property: KProperty<*>): GradleVersion {
        return GradleVersion.version(this)
    }
}


/**
 * Checks that the current Gradle version is at least [minVersion], or throws an exception.
 *
 * @param minVersion the minimum Gradle version required
 * @param lazyMessage provides the message text for the exception
 * @throws IllegalStateException if the version requirement is not met
 */
fun checkGradleVersion(minVersion: GradleVersion, lazyMessage: () -> String) =
    check(GradleVersion.current() >= minVersion, lazyMessage)


/**
 * Checks that the current Gradle version is at least [minVersion], or throws an exception.
 *
 * @param minVersion the minimum Gradle version required
 * @param pluginId the plugin ID, used in the exception message
 * @throws IllegalStateException if the version requirement is not met
 */
fun checkGradleVersion(minVersion: GradleVersion, pluginId: String) =
    checkGradleVersion(minVersion) {
        "The plugin \"$pluginId\" requires at least $minVersion" // GradleVersion.toString() looks like "Gradle 6.6.1"
    }


/**
 * Executes the given [block] only if the current Gradle version is at least [minVersion].
 *
 * @param minVersion the minimum Gradle version required
 * @param block the block to execute if the version requirement is met
 */
fun withMinGradleVersion(minVersion: GradleVersion, block: () -> Unit) {
    if (GradleVersion.current() >= minVersion) {
        block()
    }
}


/**
 * Executes the given [block] if the current Gradle version is at least [minVersion], or a [fallback]
 * otherwise.
 *
 * @param minVersion the minimum Gradle version required to execute [block]
 * @param block the block to execute if the version requirement is met
 * @param fallback the block to execute if the version requirement is not met
 *        (i.e., the current version is lower than [minVersion])
 * @return the result of [block] or [fallback]
 */
fun <T> withMinGradleVersion(
    minVersion: GradleVersion,
    block: () -> T,
    fallback: () -> T
): T =
    if (GradleVersion.current() >= minVersion) block() else fallback()
