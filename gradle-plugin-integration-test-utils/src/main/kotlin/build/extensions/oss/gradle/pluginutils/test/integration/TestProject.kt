package build.extensions.oss.gradle.pluginutils.test.integration

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import build.extensions.oss.gradle.pluginutils.test.DirectoryBuilder
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path


/**
 * A Gradle project used for integration testing.
 *
 * The project is backed by a local temporary directory, which is deleted when the [close] method
 * is called.
 *
 * It will initially place a `settings.gradle.kts` file in the project directory which contains a script
 * to set the project name. The contents of the project directory can be modified with the [directory]
 * and [file] methods.
 */
class TestProject(
    /** The name of the project. */
    val projectName: String,
    /** The project directory. */
    val projectDir: File
) : DirectoryBuilder, AutoCloseable {

    companion object {
        /**
         * Creates a temporary directory based on the project name.
         *
         * @param projectName the project name
         */
        fun createTempDir(projectName: String): File =
            Files.createTempDirectory(projectName).toFile()
    }


    /**
     * Creates a new [TestProject].
     *
     * @param projectName the project name
     */
    constructor(
        projectName: String = "test-project"
    ) : this(projectName, projectDir = createTempDir(projectName))


    init {
        file(
            "settings.gradle.kts",
            """
            rootProject.name = "$projectName"
            """.trimIndent()
        )
    }


    override val path: Path
        get() = projectDir.toPath()


    val buildDir: File
        get() = projectDir.resolve("build")


    /**
     * Execute the given action on the project directory.
     *
     * @param spec an action to execute on the project directory as a [DirectoryBuilder]
     */
    fun withProjectDir(spec: DirectoryBuilder.() -> Unit) {
        build.extensions.oss.gradle.pluginutils.test.directory(projectDir, spec)
    }


    /**
     * Creates a new subdirectory beneath the project directory.
     *
     * @param name the name of the subdirectory
     * @param spec an optional action with a [DirectoryBuilder] receiver that further specifies the
     *        contents of the subdirectory
     */
    override fun directory(name: String, spec: (DirectoryBuilder.() -> Unit)?) {
        withProjectDir {
            directory(name, spec)
        }
    }


    /**
     * Creates or modifies a file beneath the project directory.
     *
     * @param name the name of the file
     * @param contents the contents of the file
     * @param append whether to append the contents if the file already exists
     * @param charset the charset of the contents
     */
    override fun file(name: String, contents: String, append: Boolean, charset: Charset) {
        withProjectDir {
            file(name, contents, append, charset)
        }
    }


    /**
     * Creates a TestKit [GradleRunner] for running builds on this project.
     *
     * @return the [GradleRunner]
     */
    fun newGradleRunner(): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()


    /**
     * Runs a Gradle build on this project.
     *
     * @param tasks the target tasks
     * @param args additional Gradle arguments
     * @param expectFailure whether to expect the entire build to fail
     * @param expectedOutcomes map of expected [TaskOutcome]s by task path
     * @param runnerConfig additional configuration for the [GradleRunner]
     * @return the Gradle TestKit [BuildResult]
     */
    fun runGradle(
        tasks: List<String>,
        vararg args: String,
        expectFailure: Boolean = false,
        expectedOutcomes: Map<String, TaskOutcome> = tasks.associate { ":$it" to TaskOutcome.SUCCESS },
        runnerConfig: (GradleRunner.() -> Unit) = {}
    ): BuildResult {

        val allArgs = tasks.toMutableList()
        allArgs.addAll(args.asIterable())

        // Always print stacktraces when running tests
        if ("--stacktrace" !in allArgs) {
            allArgs.add("--stacktrace")
        }

        val runner = newGradleRunner()
            .withArguments(allArgs)
            .apply(runnerConfig)

        val result = if (expectFailure) runner.buildAndFail() else runner.build()

        assertThat(result).all {
            for ((taskName, expectedOutcome) in expectedOutcomes) {
                prop("task :$taskName") { it.task(taskName) }
                    .isNotNull()
                    .prop("outcome") { it.outcome }
                    .isEqualTo(expectedOutcome)
            }
        }

        return result
    }


    /**
     * Runs a Gradle build on this project with a single target task.
     *
     * @param task the target task
     * @param args additional Gradle arguments
     * @param expectFailure whether to expect the entire build to fail
     * @param expectedOutcomes map of expected [TaskOutcome]s by task path
     * @param runnerConfig additional configuration for the [GradleRunner]
     * @return the Gradle TestKit [BuildResult]
     */
    fun runGradle(
        task: String,
        vararg args: String,
        expectFailure: Boolean = false,
        expectedOutcomes: Map<String, TaskOutcome> = mapOf(":$task" to TaskOutcome.SUCCESS),
        runnerConfig: (GradleRunner.() -> Unit) = {}
    ): BuildResult = runGradle(
        listOf(task), *args,
        expectFailure = expectFailure, expectedOutcomes = expectedOutcomes, runnerConfig = runnerConfig
    )


    override fun close() {
        println("Deleting project directory $projectDir")
        projectDir.deleteRecursively()
    }
}
