package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import build.extensions.oss.gradle.pluginutils.test.directory
import build.extensions.oss.gradle.pluginutils.test.integration.util.plus
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.BiConsumer


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ProjectDir


private val namespaceKey = Any()
private val namespace = ExtensionContext.Namespace.create(namespaceKey)

private val ExtensionContext.store: ExtensionContext.Store
    get() = getStore(namespace)


private class GradleProjectDirResource : ExtensionContext.Store.CloseableResource {

    val directory: File = Files.createTempDirectory("gradle").toFile()

    override fun close() {
        directory.deleteRecursively()
    }
}

private object GradleProjectDirStoreKey


internal val ExtensionContext.projectDir: File
    get() {
        val resource = store.getOrComputeIfAbsent(GradleProjectDirStoreKey, {
            GradleProjectDirResource().also { resource ->
                initGradleProject(resource.directory)
            }
        }, GradleProjectDirResource::class.java)

        return resource.directory
    }


private fun ExtensionContext.initGradleProject(projectDir: File) {
    projectDirInitializer?.accept(projectDir, this)

    // Create a settings file if the test setup didn't create one, otherwise Gradle searches up the
    // directory hierarchy (and might actually find one)
    directory(projectDir) {
        if (!Files.exists(path.resolve("settings.gradle")) &&
            !Files.exists(path.resolve("settings.gradle.kts"))
        ) {
            file(
                name = "settings.gradle",
                contents = """
                    rootProject.name = '${projectDir.name}'
                    """.trimIndent()
            )
        }
    }
}


internal typealias ProjectDirInitializer = BiConsumer<File, ExtensionContext>


private object ProjectDirInitializerStoreKey


@Suppress("UNCHECKED_CAST")
private val ExtensionContext.projectDirInitializer: ProjectDirInitializer?
    get() = store.get(ProjectDirInitializerStoreKey) as ProjectDirInitializer?


internal fun ExtensionContext.setupProjectDir(initializer: ProjectDirInitializer) {
    store.put(
        ProjectDirInitializerStoreKey,
        this.projectDirInitializer + initializer
    )
}


internal fun ExtensionContext.setupProjectDir(initializer: (projectDir: File) -> Unit) {
    store.put(
        ProjectDirInitializerStoreKey,
        this.projectDirInitializer + ProjectDirInitializer { projectDir, _ -> initializer(projectDir) }
    )
}



/**
 * Sets up a temporary Gradle project directory, and deletes it after the test has finished.
 */
class GradleProjectDirExtension : ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.isAnnotated(ProjectDir::class.java)


    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        val projectDir = extensionContext.projectDir
        return when (parameterContext.parameter.type) {
            File::class.java -> projectDir
            Path::class.java -> projectDir.toPath()
            else -> error("Invalid parameter type")
        }
    }
}
