package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import build.extensions.oss.gradle.pluginutils.test.integration.util.plus
import java.util.function.UnaryOperator


private val namespaceKey = Any()
private val namespace = ExtensionContext.Namespace.create(namespaceKey)


private val ExtensionContext.store
    get() = getStore(namespace)


internal typealias GradleRunnerModifier = UnaryOperator<GradleRunner>


private object GradleRunnerModifierStoreKey


@Suppress("UNCHECKED_CAST")
internal val ExtensionContext.gradleRunnerModifier: GradleRunnerModifier?
    get() = store.get(GradleRunnerModifierStoreKey) as GradleRunnerModifier?


/**
 * Provides a [GradleRunner] to be injected into test methods.
 */
class GradleRunnerExtension : ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == GradleRunner::class.java


    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val projectDir = extensionContext.projectDir
        val modifier = extensionContext.gradleRunnerModifier

        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withDebug(true)
            .forwardOutput()
            .let { modifier?.apply(it) ?: it }
    }
}


/**
 * Used to modify the parameters to the [GradleRunner].
 *
 * Intended to be registered ad-hoc for test templates.
 */
internal class GradleRunnerModifierExtension(
    private val modifier: GradleRunnerModifier
) : BeforeEachCallback {

    constructor(modifier: GradleRunner.() -> GradleRunner)
            : this(GradleRunnerModifier(modifier))


    override fun beforeEach(context: ExtensionContext) {
        context.store.put(
            GradleRunnerModifierStoreKey,
            context.gradleRunnerModifier + modifier
        )
    }
}
