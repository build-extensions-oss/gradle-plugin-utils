package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.isInstanceOf
import assertk.assertions.support.expected
import org.gradle.api.Project
import org.gradle.api.Task
import kotlin.reflect.KClass


/**
 * Asserts that the Gradle [Project] contains a task with the given name and type.
 *
 * @param taskName the task name
 * @param taskType the task type
 * @return an [Assert] allowing to make further assertions on the task
 */
fun <T : Task> Assert<Project>.containsTask(taskName: String, taskType: KClass<T>) =
    transform("task \"$taskName\"") { actual ->
        actual.tasks.findByName(taskName)
            ?: expected("to contain a task named \"$taskName\"")
    }.isInstanceOf(taskType)


/**
 * Asserts that the Gradle [Project] contains a task with the given name and type.
 *
 * @param T the task type
 * @param taskName the task name
 * @return an [Assert] allowing to make further assertions on the task
 */
inline fun <reified T : Task> Assert<Project>.containsTask(taskName: String) =
    containsTask(taskName, T::class)
