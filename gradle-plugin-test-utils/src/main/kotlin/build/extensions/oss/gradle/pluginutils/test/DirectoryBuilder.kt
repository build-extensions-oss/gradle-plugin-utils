package build.extensions.oss.gradle.pluginutils.test

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.regex.Pattern
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream


@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class DirectoryBuilderDsl


/**
 * Defines a micro-DSL for setting up directories and files for tests.
 */
interface DirectoryBuilder {

    /**
     * The path of the directory in scope.
     */
    val path: Path


    /**
     * Creates a subdirectory at the given path.
     *
     * The [name] may contain multiple path elements (e.g. `path/to/subdirectory`); in this case multiple levels
     * of subdirectories are created.
     *
     * @param name the name of the subdirectory. This is interpreted as a path, relative to the current path.
     * @param spec an optional block to set up the contents of the subdirectory
     */
    @DirectoryBuilderDsl
    fun directory(name: String, spec: (DirectoryBuilder.() -> Unit)? = null)


    /**
     * Creates or modifies a file in the current directory scope.
     *
     * @param name the name of the file
     * @param contents the contents of the file, as a String
     * @param append if `true`, appends to an existing file; if `false` an existing file will be replaced
     * @param charset the character set to use
     */
    @DirectoryBuilderDsl
    fun file(name: String, contents: String, append: Boolean = false, charset: Charset = Charsets.UTF_8)
}


/**
 * Builds a directory and its contents.
 *
 * The directory may or may not already exist.
 *
 * @param basePath the absolute path of the directory
 * @param spec a block to set up the contents of the directory
 */
@DirectoryBuilderDsl
fun directory(basePath: Path, spec: DirectoryBuilder.() -> Unit) =
    DefaultDirectoryBuilder(basePath).let(spec)


/**
 * Builds a directory and its contents.
 *
 * The directory may or may not already exist.
 *
 * @param basePath the absolute path of the directory
 * @param spec a block to set up the contents of the directory
 */
@DirectoryBuilderDsl
fun directory(basePath: File, spec: DirectoryBuilder.() -> Unit) =
    directory(basePath.toPath(), spec)


/**
 * Builds a directory and its contents.
 *
 * The directory may or may not already exist.
 *
 * @param basePath the absolute path of the directory
 * @param spec a block to set up the contents of the directory
 */
@DirectoryBuilderDsl
fun directory(basePath: String, spec: DirectoryBuilder.() -> Unit) =
    directory(Paths.get(basePath), spec)


private class DefaultDirectoryBuilder(override val path: Path) : DirectoryBuilder {

    init {
        if (!Files.exists(path)) {
            Files.createDirectories(path)
        }
        check(Files.isDirectory(path)) { "Directory \"$path\" exists and is not a directory" }
    }


    override fun directory(name: String, spec: (DirectoryBuilder.() -> Unit)?) {
        val subPath = this.path.resolve(name)
        if (!Files.exists(subPath)) {
            Files.createDirectories(subPath)
        }
        check(Files.isDirectory(subPath)) { "Directory \"$subPath\" exists and is not a directory" }
        val builder = DefaultDirectoryBuilder(subPath)
        spec?.invoke(builder)
    }


    override fun file(name: String, contents: String, append: Boolean, charset: Charset) {
        val filePath = this.path.resolve(name)

        val options = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            if (append) StandardOpenOption.APPEND else StandardOpenOption.TRUNCATE_EXISTING
        )

        Files.newBufferedWriter(filePath, charset, *options)
            .use { writer ->
                writer.append(contents.trimIndent())
            }
    }
}


/**
 * Copies the given classpath resources into this directory.
 *
 * This is intended as a way to populate a temporary local directory with resources packaged with the test
 * (i.e. under `src/test/resources` or similar).
 *
 * @param prefix the prefix of the classpath resource names to copy
 * @param classLoader the [ClassLoader] to use for locating and loading resources
 */
fun DirectoryBuilder.copyResources(
    prefix: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {
    val fullPrefix = buildString {
        if (!prefix.startsWith('/')) append('/')
        append(prefix)
        if (!prefix.endsWith('/')) append('/')
    }
    val pattern = Pattern.compile("^" + Pattern.quote(fullPrefix) + ".*")

    val config = ConfigurationBuilder()
        .addClassLoaders(classLoader)
        .setScanners(Scanners.Resources)

    val reflections = Reflections(config)
    for (resourceName in reflections.getResources(pattern)) {

        requireNotNull(classLoader.getResourceAsStream(resourceName)) {
            "Resource not found: $resourceName"
        }.use { input ->
            val fileName = resourceName.removePrefix("test-resources/$prefix").removePrefix("/")
            val filePath = path.resolve(fileName)
            filePath.parent.createDirectories()

            println("Copying resource $resourceName to file $filePath")

            filePath.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
