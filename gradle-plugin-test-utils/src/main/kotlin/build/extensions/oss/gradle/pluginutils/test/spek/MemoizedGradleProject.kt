package build.extensions.oss.gradle.pluginutils.test.spek

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.MemoizedValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 * A memoized Gradle [Project]. It extends Spek's [MemoizedValue], so it can be queried by
 *
 * ```
 * val project: Project by memoized()
 * ```
 */
interface MemoizedGradleProject : MemoizedValue<Project> {

    /** The project name. */
    var projectName: String

    /**
     * Adds an initializer, e.g. for applying plugins.
     *
     * The initializer will be executed lazily when the project is actually created.
     */
    fun initializer(initializer: Project.() -> Unit)

    /**
     * Apply a Gradle plugin by type.
     *
     * The plugin will be applied lazily when the project is actually created.
     *
     * @param pluginType the type of [Plugin] to apply
     */
    fun applyPlugin(pluginType: KClass<out Plugin<Project>>) =
        initializer {
            plugins.apply(pluginType.java)
        }
}


/**
 * Apply a Gradle plugin by type.
 *
 * The plugin will be applied lazily when the project is actually created.
 *
 * @param T the type of the plugin
 */
inline fun <reified T : Plugin<Project>> MemoizedGradleProject.applyPlugin() =
    applyPlugin(T::class)


private class DefaultMemoizedGradleProject(
    private val lifecycleAware: LifecycleAware
) : MemoizedGradleProject {

    override var projectName: String = ""

    private val initializers = mutableListOf<Project.() -> Unit>()


    override fun initializer(initializer: Project.() -> Unit) {
        initializers.add(initializer)
    }


    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, Project> {
        val memoized = lifecycleAware.memoized(
            factory = {
                ProjectBuilder.builder().run {
                    projectName.takeUnless { it.isEmpty() }?.let { withName(it) }
                    build()
                }.also { project ->
                    initializers.forEach { initializer ->
                        initializer(project)
                    }
                }
            },
            destructor = { project ->
                project.projectDir.deleteRecursively()
            }
        )
        return memoized.provideDelegate(thisRef, property)
    }
}


/**
 * Creates a memoized Gradle [Project] in the current lifecycle, without any setup logic.
 *
 * Usually you will want to use this function with the `by` keyword, e.g.
 *
 * ```
 * val project by gradleProject()
 * ```
 *
 * Note that this method will always create a new project, so when accessing the project in a child lifecycle
 * scope, you should use
 *
 * ```
 * val project: Project by memoized()
 * ```
 *
 * instead.
 *
 * The project will be initialized with a temporary directory, which will be automatically cleaned up after the
 * lifecycle (using the memoized destructor).
 *
 * @receiver the [LifecycleAware]
 * @return the [MemoizedValue] for the project
 */
fun LifecycleAware.gradleProject(): MemoizedGradleProject =
    DefaultMemoizedGradleProject(this)


/**
 * Creates and sets up a memoized Gradle [Project], passing initialization logic.
 *
 * Usually you will want to use this function with the `by` keyword, e.g.
 *
 * ```
 * val project by setupGradleProject { ... }
 * ```
 *
 * Note that this method will always create a new project, so when accessing the project in a child lifecycle
 * scope, you should use
 *
 * ```
 * val project: Project by memoized()
 * ```
 *
 * instead.
 *
 * The project will be initialized with a temporary directory, which will be automatically cleaned up after the
 * lifecycle (using the memoized destructor).
 *
 * @receiver the [LifecycleAware]
 * @param block the setup logic, e.g. for applying plugins or setting the project name
 * @return the [MemoizedValue] for the project
 */
fun LifecycleAware.setupGradleProject(block: MemoizedGradleProject.() -> Unit): MemoizedValue<Project> {

    val project: Project by gradleProject().also(block)

    return memoized()
}
