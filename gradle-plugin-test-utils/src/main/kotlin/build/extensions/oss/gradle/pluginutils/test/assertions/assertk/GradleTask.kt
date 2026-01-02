package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.support.expected
import assertk.assertions.support.show
import org.gradle.api.Task
import build.extensions.oss.gradle.pluginutils.test.isSkipped


/**
 * Transforms the assertion to the task's dependencies.
 *
 * @return an [Assert] that allows to make assertions on the task dependencies
 */
val Assert<Task>.taskDependencies: Assert<Set<Task>>
    get() = transform { actual ->
        actual.taskDependencies.getDependencies(actual)
    }


/**
 * Asserts that the task has a dependency on another task.
 *
 * The task may have other dependencies besides the given task.
 *
 * @param taskName the name of the other task
 */
fun Assert<Task>.hasTaskDependency(taskName: String) = given { actual ->
    val dependencies = actual.taskDependencies.getDependencies(actual)
    if (dependencies.none { it.name == taskName }) {
        expected("to have a dependency on task \"${taskName}\", but dependencies were: ${show(dependencies)}")
    }
}


/**
 * Asserts that the task has a single dependency on another task.
 *
 * @param taskName the name of the other task
 */
fun Assert<Task>.hasOnlyTaskDependency(taskName: String) = given { actual ->
    val dependencies = actual.taskDependencies.getDependencies(actual)
    if (dependencies.size != 1 || dependencies.firstOrNull()?.name != taskName) {
        expected("to have a single dependency on task \"${taskName}\", but dependencies were: ${show(dependencies)}")
    }
}


/**
 * Asserts that the task has dependencies on the given other tasks.
 *
 * @param taskNames the names of the other tasks
 * @param exactly if `true`, also checks that the task does not have any other dependencies besides [taskNames]
 */
fun Assert<Task>.hasTaskDependencies(vararg taskNames: String, exactly: Boolean = false) = given { actual ->
    val dependencies = actual.taskDependencies.getDependencies(actual)

    val dependencyTaskNames = dependencies.map { it.name }.toSet()

    val assert = assertThat(dependencyTaskNames, name = "task \"${actual.name}\" dependencies")

    if (exactly) {
        assert.containsOnly(*taskNames)
    } else {
        assert.containsAll(*taskNames)
    }
}


/**
 * Asserts that the task does not have a dependency on the given task.
 *
 * @param taskName the name of the other task
 */
fun Assert<Task>.doesNotHaveTaskDependency(taskName: String) = given { actual ->
    val dependencies = actual.taskDependencies.getDependencies(actual)
    if (dependencies.any { it.name == taskName }) {
        expected("to have no dependency on task \"${taskName}\", but dependencies were: ${show(dependencies)}")
    }
}


/**
 * Asserts that the task is skipped.
 *
 * @see Task.isSkipped
 */
fun Assert<Task>.isSkipped() = given { actual ->
    if (!actual.isSkipped()) {
        expected("to be skipped, but was not skipped")
    }
}


/**
 * Asserts that the task is not skipped.
 *
 * @see Task.isSkipped
 */
fun Assert<Task>.isNotSkipped() = given { actual ->
    if (actual.isSkipped()) {
        expected("not to be skipped, but was skipped")
    }
}
