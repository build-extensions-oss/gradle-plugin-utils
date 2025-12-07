package build.extensions.oss.gradle.pluginutils.test

import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskOutputs
import org.gradle.internal.operations.BuildOperationContext
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.operations.RunnableBuildOperation
import org.gradle.workers.WorkerExecutor


enum class TaskOutcome {
    SUCCESS,
    FAILED,
    UP_TO_DATE,
    SKIPPED,
}

/**
 * The class is needed for this file only. Due to internal changes in Gradle with tasks invocations, we must construct
 * the operation explicitly. So,
 */
private class RunnableBuildOperationWrapper(private val functionToRun: () -> TaskOutcome) : RunnableBuildOperation {
    // we assume that the result will be produced only once. The class doesn't disallow setting it externally - just don't do that
    var result: TaskOutcome? = null

    /**
     * Invokes the Gradle action. We also have to remember tasks output, so we use try...catch as well
     */
    override fun run(buildOperationContext: BuildOperationContext) {
        try {
            result = functionToRun()
        } catch (ex: Exception) {
            result = TaskOutcome.FAILED

            throw ex
        }

        buildOperationContext.setResult(result)
    }

    override fun description(): BuildOperationDescriptor.Builder {
        return BuildOperationDescriptor.displayName("ignored")
    }
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
@Deprecated(message = "This function uses internal Gradle API to run tasks, which can be changed between versions. Instead, Gradle functional tests should be used.")
fun Task.execute(
    checkUpToDate: Boolean = true, checkOnlyIf: Boolean = true, rethrowExceptions: Boolean = true
): TaskOutcome {
    this as TaskInternal

    // resolve internal Gradle service registry (aka internal service locator)
    val services = (project as ProjectInternal).services

    val buildOperationExecutor: BuildOperationExecutor = services[BuildOperationExecutor::class.java]
    val workerExecutor = services[WorkerExecutor::class.java]

    // construct new operation. This isn't a correct code, it just hacks up what is done inside Gradle
    val newOperation = RunnableBuildOperationWrapper {
        if (checkOnlyIf && !onlyIf.isSatisfiedBy(this)) {
            return@RunnableBuildOperationWrapper TaskOutcome.SKIPPED
        }

        // workaround up-to-date task
        if (checkUpToDate) {
            val upToDateSpec = outputs.upToDateSpec
            val upToDate = !upToDateSpec.isEmpty && upToDateSpec.isSatisfiedBy(this)
            if (upToDate) {
                didWork = false
                return@RunnableBuildOperationWrapper TaskOutcome.UP_TO_DATE
            }
        }

        // execute all actions scheduled. Please note - this isn't fully correct code, because we don't process exceptions properly
        actions.forEach {
            it.execute(this)
        }

        workerExecutor.await()
        // return results
        return@RunnableBuildOperationWrapper if (didWork) TaskOutcome.SUCCESS else TaskOutcome.UP_TO_DATE
    }

    // run all operations via that queue
    buildOperationExecutor.runAll { buildOperationQueue ->
        buildOperationQueue.add(newOperation)
    }

    // the result was set implicitly (bad approach, very bad), however it is good enough for tests
    return newOperation.result!!
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
