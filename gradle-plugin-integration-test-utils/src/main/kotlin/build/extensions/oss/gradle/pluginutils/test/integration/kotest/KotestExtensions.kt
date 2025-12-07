package build.extensions.oss.gradle.pluginutils.test.integration.kotest

import io.kotest.core.TestConfiguration
import build.extensions.oss.gradle.pluginutils.test.integration.TestProject


/**
 * Creates a [TestProject] with the given name and registers it to be closed after
 * the spec is completed.
 *
 * @param name the name of the project
 */
fun TestConfiguration.testProject(name: String = "test-project") =
    autoClose(TestProject(name))
