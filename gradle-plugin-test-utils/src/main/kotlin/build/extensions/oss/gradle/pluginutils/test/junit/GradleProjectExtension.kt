package build.extensions.oss.gradle.pluginutils.test.junit

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.BiConsumer
import java.util.function.Consumer


/**
 * Marks the test as a Gradle project test. Applies the [GradleProjectExtension].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
@ExtendWith(GradleProjectExtension::class)
annotation class GradleProjectTest


/**
 * Indicates the name of the Gradle project that should be used for tests in this scope.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class GradleProjectName(
    /** The name of the project. */
    val value: String
)


/**
 * Indicates that the annotated method should be called to customize the Gradle [ProjectBuilder].
 *
 * The annotated method must take a single parameter of type [ProjectBuilder].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SetupProjectBuilder


private typealias ProjectBuilderCustomizer = BiConsumer<ProjectBuilder, ExtensionContext>


private object NamespaceKey


private val namespace: ExtensionContext.Namespace =
    ExtensionContext.Namespace.create(NamespaceKey)


private val ExtensionContext.store: ExtensionContext.Store
    get() = getStore(namespace)


private object ProjectBuilderCustomizerStoreKey


@Suppress("UNCHECKED_CAST")
private val ExtensionContext.projectBuilderCustomizer: ProjectBuilderCustomizer?
    get() = store.get(ProjectBuilderCustomizerStoreKey) as ProjectBuilderCustomizer?


class GradleProjectExtension : BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private companion object {

        private fun nameCustomizer(projectName: String): ProjectBuilderCustomizer =
            ProjectBuilderCustomizer { projectBuilder, _ ->
                projectBuilder.withName(projectName)
            }
    }


    private var customizer: Consumer<ProjectBuilder> = Consumer { }


    fun withProjectBuilder(customizer: Consumer<ProjectBuilder>) = apply {
        this.customizer = this.customizer.andThen(customizer)
    }


    fun withProjectBuilder(customizer: ProjectBuilder.() -> Unit) = apply {
        withProjectBuilder(Consumer(customizer))
    }


    fun useProjectName(projectName: String) {
        withProjectBuilder { withName(projectName) }
    }


    @ExperimentalStdlibApi
    override fun beforeAll(context: ExtensionContext) {

        val nameAnnotation = context.requiredTestClass.getAnnotation(GradleProjectName::class.java)
        val nameCustomizer = if (nameAnnotation != null) {
            nameCustomizer(nameAnnotation.value)
        } else null

        val customizerFromMethods = context.requiredTestClass.methods.asSequence()
            .filter { it.isAnnotationPresent(SetupProjectBuilder::class.java) }
            .map { method ->
                check(method.parameterTypes.size == 1 &&
                            method.parameterTypes.single() == ProjectBuilder::class.java) {
                    "A method annotated with @SetupProjectBuilder must have a single parameter of type ProjectBuilder"
                }
                @Suppress("USELESS_CAST") // false positive, removing cast gives compile error
                MethodProjectBuilderCustomizer(method) as BiConsumer<ProjectBuilder, ExtensionContext>
            }
            .reduceOrNull { acc, customizer ->
                acc.andThen(customizer)
            }

        val combinedCustomizer = context.projectBuilderCustomizer
            .andThen(nameCustomizer)
            .andThen(customizerFromMethods)
        if (combinedCustomizer != null) {
            context.store.put(ProjectBuilderCustomizerStoreKey, combinedCustomizer)
        }
    }


    private class MethodProjectBuilderCustomizer(
        private val method: Method
    ) : BiConsumer<ProjectBuilder, ExtensionContext> {

        override fun accept(projectBuilder: ProjectBuilder, extensionContext: ExtensionContext) {
            if (Modifier.isStatic(method.modifiers)) {
                method.invoke(null, projectBuilder)
            } else {
                method.invoke(extensionContext.requiredTestInstance, projectBuilder)
            }
        }
    }


    override fun beforeEach(context: ExtensionContext) {
        val contextCustomizer = context.projectBuilderCustomizer

        val project = ProjectBuilder.builder()
            .also { builder ->
                this.customizer.accept(builder)
                contextCustomizer?.accept(builder, context)
            }
            .build()

        context.store.put(Project::class.java, project)
    }


    override fun afterEach(context: ExtensionContext) {
        context.store.get(Project::class.java, Project::class.java)
            ?.projectDir?.deleteRecursively()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == Project::class.java


    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return extensionContext.store.get(Project::class.java, Project::class.java)
    }
}


private fun <T, U> BiConsumer<T, U>?.andThen(other: BiConsumer<T, U>?): BiConsumer<T, U>? =
    when {
        this == null -> other
        other == null -> this
        else -> {
            BiConsumer { t, u -> this.accept(t, u); other.accept(t, u); }
        }
    }
