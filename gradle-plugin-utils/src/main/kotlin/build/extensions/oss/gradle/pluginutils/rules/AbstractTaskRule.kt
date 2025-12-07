package build.extensions.oss.gradle.pluginutils.rules

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer


/**
 * Base class for a rule that constructs tasks based on a domain object.
 */
abstract class AbstractTaskRule<S : Named, T : Task>(
    /** The type of the tasks that this rule will construct. */
    taskType: Class<T>,
    /** The tasks of the Gradle project. */
    protected val tasks: TaskContainer,
    /** The container of source domain objects. */
    sourceContainer: NamedDomainObjectCollection<S>,
    /** The naming pattern for the tasks. */
    namePattern: RuleNamePattern
) : AbstractPatternRule<S, T>(taskType, tasks, sourceContainer, namePattern)


/**
 * Base class for a rule that constructs task based on the combination of two domain objects.
 */
abstract class AbstractTaskRule2<S1 : Named, S2 : Named, T : Task>(
    /** The type of the tasks that this rule will construct. */
    taskType: Class<T>,
    /** The tasks of the Gradle project. */
    protected val tasks: TaskContainer,
    /** The container of the first type of source domain objects. */
    sourceContainer1: NamedDomainObjectCollection<S1>,
    /** The container of the second type of source domain objects. */
    sourceContainer2: NamedDomainObjectCollection<S2>,
    /** The naming pattern for the tasks. */
    namePattern: RuleNamePattern2
) : AbstractPatternRule2<S1, S2, T>(taskType, tasks, sourceContainer1, sourceContainer2, namePattern)
