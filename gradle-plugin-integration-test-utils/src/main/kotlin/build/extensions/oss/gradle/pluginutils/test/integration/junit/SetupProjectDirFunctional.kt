package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import build.extensions.oss.gradle.pluginutils.test.DirectoryBuilder
import build.extensions.oss.gradle.pluginutils.test.directory


/**
 * Creates a JUnit [Extension] that performs some setup on the project directory.
 *
 * The result of this function must be stored in a field and annotated with [RegisterExtension].
 */
fun setupProjectDir(block: DirectoryBuilder.() -> Unit): Extension =
    SetupProjectDirFunctionalExtension(block)


private class SetupProjectDirFunctionalExtension(
    private val block: DirectoryBuilder.() -> Unit
) : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        context.setupProjectDir { projectDir ->
            directory(projectDir, block)
        }
    }
}
