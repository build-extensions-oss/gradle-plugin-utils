package build.extensions.oss.gradle.pluginutils.test

import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskOutputs
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.workers.WorkerExecutor


enum class TaskOutcome {
    SUCCESS,
    FAILED,
    UP_TO_DATE,
    SKIPPED,
}


/**
 * Executes a task.
 *
 * This works in a very limited way (e.g. it does not consider task dependencies), but it should be enough to have
 * Gradle run the task's actions in a somewhat realistic way.
 *
 * The [checkUpToDate] parameter can be used to control whether to run up-to-date checks, as defined by custom
 * [TaskOutputs.upToDateWhen] blocks. If the up-to-date checks are evaluated and the task should be considered
 * up-to-date, this function will set the [didWork][Task.getDidWork] flag on the task so it can be verified by a test.
 *
 * @receiver the [Task] to execute
 * @param checkUpToDate if `true`, run up-to-date checks first
 * @param checkOnlyIf if `true`, run only-if checks first (includes checking the [enabled][Task.getEnabled] property)
 * @param rethrowExceptions if `true`, re-throw any exceptions that occur in the task. If `false`, return an
 *        outcome of [TaskOutcome.FAILED] if the task throws an exception
 * @return a [TaskOutcome] indicating the outcome of the task
 */
fun Task.execute(
    checkUpToDate: Boolean = true, checkOnlyIf: Boolean = true, rethrowExceptions: Boolean = true
): TaskOutcome {

    this as TaskInternal

    val services = (project as ProjectInternal).services

    val buildOperationExecutor = services[BuildOperationExecutor::class.java]
    val workerExecutor = services[WorkerExecutor::class.java]

    val buildOperation = buildOperationExecutor.start(BuildOperationDescriptor.displayName(name))
    var buildOperationResult = BuildResult(project.gradle, null)

    try {
        if (checkOnlyIf && !onlyIf.isSatisfiedBy(this)) {
            return TaskOutcome.SKIPPED
        }

        if (checkUpToDate) {
            val upToDateSpec = outputs.upToDateSpec
            val upToDate = !upToDateSpec.isEmpty && upToDateSpec.isSatisfiedBy(this)
            if (upToDate) {
                didWork = false
                return TaskOutcome.UP_TO_DATE
            }
        }

        actions.forEach {
            it.execute(this)
        }

        workerExecutor.await()
        return if (didWork) TaskOutcome.SUCCESS else TaskOutcome.UP_TO_DATE

    } catch (ex: Exception) {
        buildOperationResult = BuildResult(project.gradle, ex)
        if (rethrowExceptions) {
            throw ex
        } else {
            buildOperation.failed(ex)
            return TaskOutcome.FAILED
        }
    } finally {
        buildOperation.setResult(buildOperationResult)
    }
}


/**
 * Evaluates the task's [Task.onlyIf] specs to check if the task is skipped.
 *
 * @receiver the [Task] to check
 * @return `true` if the task is skipped
 */
fun Task.isSkipped(): Boolean {
    this as TaskInternal
    return !onlyIf.isSatisfiedBy(this)
}
