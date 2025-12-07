package build.extensions.oss.gradle.pluginutils.test

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal


/**
 * Evaluates the project.
 *
 * Evaluation of the project is usually triggered by Gradle, so it cannot be triggered using the public API.
 * Still, it may be useful for testing logic that involves late configuration such as a [Project.afterEvaluate]
 * block.
 *
 * @receiver the [Project]
 */
fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}
