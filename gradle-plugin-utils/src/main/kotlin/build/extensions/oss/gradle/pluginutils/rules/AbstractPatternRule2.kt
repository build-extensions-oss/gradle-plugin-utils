package build.extensions.oss.gradle.pluginutils.rules

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Rule
import kotlin.reflect.KClass


/**
 * Base class for a rule that creates a domain object of type [T] from two domain objects of type [S1] and [S2]
 * if it matches a certain name pattern.
 */
abstract class AbstractPatternRule2<S1 : Named, S2 : Named, T : Any>(
    /** A function that creates and configures the target object on a successful name match. */
    private val targetCreator: (name: String, configureAction: Action<T>) -> Unit,
    /** The container for the first type of source object. */
    protected val sourceContainer1: NamedDomainObjectCollection<S1>,
    /** The container for the second type of source object. */
    protected val sourceContainer2: NamedDomainObjectCollection<S2>,
    /** The pattern that is used for matching the source and target names. */
    protected val namePattern: RuleNamePattern2
) : AbstractRule() {

    constructor(
        targetContainer: NamedDomainObjectContainer<T>,
        sourceContainer1: NamedDomainObjectCollection<S1>,
        sourceContainer2: NamedDomainObjectCollection<S2>,
        namePattern: RuleNamePattern2
    ) : this(
        { name, action -> targetContainer.create(name, action) }, sourceContainer1, sourceContainer2, namePattern
    )


    constructor(
        targetType: Class<T>,
        targetContainer: PolymorphicDomainObjectContainer<in T>,
        sourceContainer1: NamedDomainObjectCollection<S1>,
        sourceContainer2: NamedDomainObjectCollection<S2>,
        namePattern: RuleNamePattern2
    ) : this(
        { name, action -> targetContainer.create(name, targetType, action) }, sourceContainer1, sourceContainer2,
        namePattern
    )


    override fun getDescription(): String = namePattern.toString()


    final override fun apply(targetName: String) {
        if (namePattern.matches(targetName)) {
            namePattern.findSources(targetName, sourceContainer1, sourceContainer2)
                ?.let { (source1, source2) ->
                    targetCreator(targetName, Action { it.configureFrom(source1, source2) })
                }
        }
    }


    /**
     * Configures the target object after creation.
     *
     * @param source1 the first source object
     * @param source2 the second source object
     */
    protected abstract fun T.configureFrom(source1: S1, source2: S2)
}


fun <T : Any, S1 : Named, S2 : Named> NamedDomainObjectContainer<T>.addPatternRule(
    namePattern: RuleNamePattern2,
    sourceContainer1: NamedDomainObjectCollection<S1>,
    sourceContainer2: NamedDomainObjectContainer<S2>,
    configure: T.(S1, S2) -> Unit
) {
    val rule: Rule = object : AbstractPatternRule2<S1, S2, T>(
        this, sourceContainer1, sourceContainer2, namePattern
    ) {
        override fun T.configureFrom(source1: S1, source2: S2) {
            configure(source1, source2)
        }
    }
    addRule(rule)
}


fun <T : Any, S1 : Named, S2 : Named> PolymorphicDomainObjectContainer<in T>.addPatternRule(
    namePattern: RuleNamePattern2,
    targetType: KClass<T>,
    sourceContainer1: NamedDomainObjectCollection<S1>,
    sourceContainer2: NamedDomainObjectCollection<S2>,
    configure: T.(S1, S2) -> Unit
) {
    val rule: Rule = object : AbstractPatternRule2<S1, S2, T>(
        targetType.javaObjectType, this, sourceContainer1, sourceContainer2, namePattern
    ) {
        override fun T.configureFrom(source1: S1, source2: S2) {
            configure(source1, source2)
        }
    }
    addRule(rule)
}
