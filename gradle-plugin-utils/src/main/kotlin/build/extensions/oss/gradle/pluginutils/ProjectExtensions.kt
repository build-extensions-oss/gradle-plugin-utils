package build.extensions.oss.gradle.pluginutils

import org.gradle.api.Project
import org.gradle.api.provider.Provider


/**
 * A [Provider] that returns the project's [version][Project.getVersion] as a String.
 */
val Project.versionProvider: Provider<String>
    get() = provider { version.toString() }
