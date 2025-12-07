package build.extensions.oss.gradle.pluginutils.rules

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Rule
import kotlin.reflect.KClass

/**
 * Base class for a rule that creates a domain object of type [T] from a domain object of type [S]
 * if it matches a certain name pattern.
 */
abstract class AbstractPatternRule<S : Named, T : Any>
/**
 * Constructs an instance of [AbstractPatternRule], creating target objects using a creator function.
 */
constructor(
    /** A function that creates and configures the target object on a successful name match. */
    private val targetCreator: (name: String, configureAction: Action<T>) -> Unit,
    /** The source container. */
    protected val sourceContainer: NamedDomainObjectCollection<S>,
    /** The pattern that is used for matching the source and target names. */
    protected val namePattern: RuleNamePattern
) : AbstractRule() {

    /**
     * Constructs an instance of [AbstractPatternRule] for a target [NamedDomainObjectContainer],
     * creating target objects using [NamedDomainObjectContainer.create].
     */
    constructor(
        /** The target container. */
        targetContainer: NamedDomainObjectContainer<T>,
        /** The source container. */
        sourceContainer: NamedDomainObjectCollection<S>,
        /** The pattern that is used for matching the source and target names. */
        namePattern: RuleNamePattern
    ) : this(
        { name, action -> targetContainer.create(name, action) }, sourceContainer, namePattern
    )


    /**
     * Constructs an instance of [AbstractPatternRule] for a target [PolymorphicDomainObjectContainer],
     * creating target objects using [PolymorphicDomainObjectContainer.create].
     */
    constructor(
        /** The type of target objects that this rule creates. */
        targetType: Class<T>,
        /** The target container. */
        targetContainer: PolymorphicDomainObjectContainer<in T>,
        /** The source container. */
        sourceContainer: NamedDomainObjectCollection<S>,
        namePattern: RuleNamePattern
    ) : this(
        { name, action -> targetContainer.create(name, targetType, action) }, sourceContainer, namePattern
    )


    override fun getDescription(): String = namePattern.toString()


    final override fun apply(targetName: String) {
        if (namePattern.matches(targetName)) {
            namePattern.findSource(targetName, sourceContainer)
                ?.let { source ->
                    targetCreator(targetName, Action { it.configureFrom(source) })
                }
        }
    }


    /**
     * Configures the target object after creation.
     *
     * @param source the source object
     */
    protected abstract fun T.configureFrom(source: S)
}


fun <T : Any, S : Named> NamedDomainObjectContainer<T>.addPatternRule(
    namePattern: RuleNamePattern,
    sourceContainer: NamedDomainObjectCollection<S>,
    configure: T.(S) -> Unit
) {
    val rule: Rule = object : AbstractPatternRule<S, T>(
        this, sourceContainer, namePattern
    ) {
        override fun T.configureFrom(source: S) {
            configure(source)
        }
    }
    addRule(rule)
}


fun <T : Any, S : Named> PolymorphicDomainObjectContainer<in T>.addPatternRule(
    namePattern: RuleNamePattern,
    targetType: KClass<T>,
    sourceContainer: NamedDomainObjectCollection<S>,
    configure: T.(S) -> Unit
) {
    val rule: Rule = object : AbstractPatternRule<S, T>(
        targetType.javaObjectType, this, sourceContainer, namePattern
    ) {
        override fun T.configureFrom(source: S) {
            configure(source)
        }
    }
    addRule(rule)
}
