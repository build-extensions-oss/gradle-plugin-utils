package build.extensions.oss.gradle.pluginutils.rules

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Rule
import kotlin.reflect.KClass

/**
 * Base class for a rule that creates a domain object of type [T] from two domain objects of type [SOuter] and [SInner]
 * if it matches a certain name pattern, where the collection of [SInner] objects is derived from each [SOuter] item
 * (i.e., a composition relationship)
 */
abstract class AbstractPatternRuleOuterInner<SOuter : Named, SInner : Named, T : Any>(
    /** A function that creates and configures the target object on a successful name match. */
    private val targetCreator: (name: String, configureAction: Action<T>) -> Unit,
    /** The container for the first type of source object. */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val outerSourceContainer: NamedDomainObjectCollection<SOuter>,
    /** The container for the second type of source object. */
    private val innerSourceContainerFunction: (SOuter) -> NamedDomainObjectCollection<SInner>,
    /** The pattern that is used for matching the source and target names. */
    protected val namePattern: RuleNamePattern2
) : AbstractRule() {

    constructor(
        targetContainer: NamedDomainObjectContainer<T>,
        outerSourceContainer: NamedDomainObjectCollection<SOuter>,
        innerSourceContainerFunction: (SOuter) -> NamedDomainObjectCollection<SInner>,
        namePattern: RuleNamePattern2
    ) : this(
        { name, action -> targetContainer.create(name, action) },
        outerSourceContainer, innerSourceContainerFunction, namePattern
    )


    constructor(
        targetType: Class<T>,
        targetContainer: PolymorphicDomainObjectContainer<in T>,
        outerSourceContainer: NamedDomainObjectCollection<SOuter>,
        innerSourceContainerFunction: (SOuter) -> NamedDomainObjectCollection<SInner>,
        namePattern: RuleNamePattern2
    ) : this(
        { name, action -> targetContainer.create(name, targetType, action) },
        outerSourceContainer, innerSourceContainerFunction, namePattern
    )


    override fun getDescription(): String = namePattern.toString()


    final override fun apply(targetName: String) {
        if (namePattern.matches(targetName)) {
            namePattern.findSources(targetName, outerSourceContainer, innerSourceContainerFunction)
                ?.let { (source1, source2) ->
                    targetCreator(targetName, Action { it.configureFrom(source1, source2) })
                }
        }
    }


    /**
     * Configures the target object after creation.
     *
     * @param outerSource the outer source object
     * @param innerSource the inner source object
     */
    protected abstract fun T.configureFrom(outerSource: SOuter, innerSource: SInner)
}


fun <T : Any, SOuter : Named, SInner : Named> NamedDomainObjectContainer<T>.addPatternRule(
    namePattern: RuleNamePattern2,
    outerSourceContainer: NamedDomainObjectCollection<SOuter>,
    innerSourceContainerFunction: (SOuter) -> NamedDomainObjectCollection<SInner>,
    configure: T.(SOuter, SInner) -> Unit
) {
    val rule: Rule = object : AbstractPatternRuleOuterInner<SOuter, SInner, T>(
        this, outerSourceContainer, innerSourceContainerFunction, namePattern
    ) {
        override fun T.configureFrom(outerSource: SOuter, innerSource: SInner) {
            configure(outerSource, innerSource)
        }
    }
    addRule(rule)
}


fun <T : Any, SOuter : Named, SInner : Named> PolymorphicDomainObjectContainer<in T>.addPatternRule(
    namePattern: RuleNamePattern2,
    targetType: KClass<T>,
    outerSourceContainer: NamedDomainObjectCollection<SOuter>,
    innerSourceContainerFunction: (SOuter) -> NamedDomainObjectCollection<SInner>,
    configure: T.(SOuter, SInner) -> Unit
) {
    val rule: Rule = object : AbstractPatternRuleOuterInner<SOuter, SInner, T>(
        targetType.javaObjectType, this, outerSourceContainer, innerSourceContainerFunction, namePattern
    ) {
        override fun T.configureFrom(outerSource: SOuter, innerSource: SInner) {
            configure(outerSource, innerSource)
        }
    }
    addRule(rule)
}
