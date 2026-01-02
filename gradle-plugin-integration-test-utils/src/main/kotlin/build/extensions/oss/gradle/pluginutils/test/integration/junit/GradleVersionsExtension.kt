package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.gradle.api.logging.Logging
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.util.AnnotationUtils
import java.lang.reflect.AnnotatedElement
import java.util.Optional
import java.util.stream.Stream
import kotlin.streams.asStream


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@ExtendWith(GradleVersionsExtension::class)
annotation class GradleVersions(
    vararg val value: String
)


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@TestTemplate
annotation class TestEachGradleVersion


private val namespaceKey = Any()
private val namespace = ExtensionContext.Namespace.create(namespaceKey)


private val ExtensionContext.store
    get() = getStore(namespace)


private object GradleVersionsStoreKey


@Suppress("UNCHECKED_CAST")
private var ExtensionContext.gradleVersions: List<String>
    get() = (store.get(GradleVersionsStoreKey) as List<String>?).orEmpty()
    set(value) {
        store.put(GradleVersionsStoreKey, value)
    }


internal class GradleVersionsExtension : BeforeAllCallback, BeforeEachCallback, TestTemplateInvocationContextProvider {

    private val logger = Logging.getLogger(javaClass)


    override fun beforeAll(context: ExtensionContext) {
        processVersionsAnnotation(context, context.testClass)
    }


    override fun beforeEach(context: ExtensionContext) {
        processVersionsAnnotation(context, context.testMethod)
    }


    private fun processVersionsAnnotation(context: ExtensionContext, annotated: Optional<out AnnotatedElement>) {
        annotated.map { it.getAnnotation(GradleVersions::class.java) }
            .ifPresent { annotation ->
                context.gradleVersions = annotation.parseVersions()
            }
    }


    // suppress false positive
    @Suppress("UnusedPrivateMember")
    private fun GradleVersions.parseVersions(): List<String> =
        value.asSequence()
            .flatMap { parseVersions(it) }
            .toList()


    private fun parseVersions(versions: String): Sequence<String> =
        versions.splitToSequence(',').map { it.trim() }


    override fun supportsTestTemplate(context: ExtensionContext): Boolean =
        AnnotationUtils.isAnnotated(context.requiredTestMethod, TestEachGradleVersion::class.java)


    override fun provideTestTemplateInvocationContexts(
        context: ExtensionContext
    ): Stream<TestTemplateInvocationContext> {

        val versions = context.gradleVersions

        if (versions.isEmpty()) {
            logger.warn(
                "Test method {} is annotated with @TestEachGradleVersion, but no Gradle versions are specified.",
                context.requiredTestMethod
            )
            return Stream.empty()
        }

        return versions.asSequence()
            .map { gradleVersion ->
                GradleVersionTestTemplateInvocationContext(gradleVersion)
            }
            .asStream()
    }


    private class GradleVersionTestTemplateInvocationContext(
        private val gradleVersion: String
    ) : TestTemplateInvocationContext {

        private val logger = Logging.getLogger(javaClass)


        override fun getDisplayName(invocationIndex: Int): String =
            "[Gradle $gradleVersion]"


        override fun getAdditionalExtensions(): List<Extension> =
            listOf(
                GradleRunnerModifierExtension {
                    logger.lifecycle("Running with Gradle version {}", gradleVersion)
                    withGradleVersion(gradleVersion)
                        .withDebug(false)
                }
            )
    }
}
