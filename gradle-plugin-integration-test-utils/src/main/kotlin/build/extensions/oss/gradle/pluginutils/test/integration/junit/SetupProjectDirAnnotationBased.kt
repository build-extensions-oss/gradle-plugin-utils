package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import build.extensions.oss.gradle.pluginutils.test.DirectoryBuilder
import build.extensions.oss.gradle.pluginutils.test.directory
import build.extensions.oss.gradle.pluginutils.test.integration.util.plus
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Path


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SetupProjectDir


internal class SetupProjectDirAnnotationBasedExtension : BeforeAllCallback {

    private companion object {

        const val ErrorMessage = "A method annotated with @SetupProjectDir must have a single " +
                "parameter of type File, Path or DirectoryBuilder"
    }


    @ExperimentalStdlibApi
    override fun beforeAll(context: ExtensionContext) {

        val initializer = context.requiredTestClass.methods.asSequence()
            .filter { method ->
                method.isAnnotationPresent(SetupProjectDir::class.java)
            }
            .map { method ->
                check(method.parameterTypes.size == 1) { ErrorMessage }

                val initializer = when (method.parameterTypes.single()) {
                    File::class.java -> FileMethodProjectDirInitializer(method)
                    Path::class.java -> PathMethodProjectDirInitializer(method)
                    DirectoryBuilder::class.java -> DirectoryBuilderMethodProjectDirInitializer(method)
                    else -> error(ErrorMessage)
                }

                @Suppress("USELESS_CAST") // false positive, removing cast gives compile error
                initializer as ProjectDirInitializer?
            }
            .reduceOrNull { acc, initializer -> acc + initializer }

        if (initializer != null) {
            context.setupProjectDir(initializer)
        }
    }
}


private abstract class AbstractMethodProjectDirInitializer<T : Any>(
    private val method: Method
) : ProjectDirInitializer {

    init {
        assert(method.parameterTypes.size == 1)
    }


    protected abstract fun invoke(projectDir: File, invokeMethod: (T) -> Unit)


    final override fun accept(projectDir: File, context: ExtensionContext) {
        val target = if (Modifier.isStatic(method.modifiers)) null else context.requiredTestInstance

        invoke(projectDir) { arg ->
            method.invoke(target, arg)
        }
    }
}


private class FileMethodProjectDirInitializer(method: Method) : AbstractMethodProjectDirInitializer<File>(method) {

    init {
        assert(method.parameterTypes.single() == File::class.java)
    }


    override fun invoke(projectDir: File, invokeMethod: (File) -> Unit) {
        invokeMethod(projectDir)
    }
}


private class PathMethodProjectDirInitializer(method: Method) : AbstractMethodProjectDirInitializer<Path>(method) {

    init {
        assert(method.parameterTypes.single() == Path::class.java)
    }


    override fun invoke(projectDir: File, invokeMethod: (Path) -> Unit) {
        invokeMethod(projectDir.toPath())
    }
}


private class DirectoryBuilderMethodProjectDirInitializer(
    method: Method
) : AbstractMethodProjectDirInitializer<DirectoryBuilder>(method) {

    init {
        assert(method.parameterTypes.single() == DirectoryBuilder::class.java)
    }


    override fun invoke(projectDir: File, invokeMethod: (DirectoryBuilder) -> Unit) {
        directory(projectDir, invokeMethod)
    }
}
