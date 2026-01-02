package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner


fun GradleRunner.runGradle(vararg args: String, expectFailure: Boolean = false): BuildResult =
    withArguments(*args, "--stacktrace")
        .run {
            if (expectFailure) buildAndFail() else build()
        }
