package build.extensions.oss.gradle.pluginutils.test.spek

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.MemoizedValue
import build.extensions.oss.gradle.pluginutils.decapitalize
import kotlin.reflect.KClass


/**
 * Creates a memoized Gradle task.
 *
 * @param taskType the type of task to create
 * @param name the task name
 * @param config an optional block for configuring the task
 */
fun <T : Task> LifecycleAware.gradleTask(
    taskType: KClass<T>,
    name: String? = null,
    config: T.() -> Unit = {}
): MemoizedValue<T> {
    val project: Project by memoized()
    val actualName = name ?: (taskType.simpleName?.decapitalize() ?: "task")

    return memoized<T> {
        project.tasks.create(actualName, taskType.java, Action(config))
    }
}


/**
 * Creates a memoized Gradle task.
 *
 * @param T the type of task to create
 * @param name the task name
 * @param config an optional block for configuring the task
 */
inline fun <reified T : Task> LifecycleAware.gradleTask(
    name: String? = null, noinline config: T.() -> Unit = {}
) =
    gradleTask(T::class, name, config)
