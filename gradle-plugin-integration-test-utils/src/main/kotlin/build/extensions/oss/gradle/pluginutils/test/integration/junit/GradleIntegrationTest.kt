package build.extensions.oss.gradle.pluginutils.test.integration.junit

import org.junit.jupiter.api.extension.ExtendWith


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
@ExtendWith(
    GradleProjectDirExtension::class,
    GradleRunnerExtension::class,
    SetupProjectDirAnnotationBasedExtension::class
)
annotation class GradleIntegrationTest
