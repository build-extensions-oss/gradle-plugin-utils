package build.extensions.oss.gradle.pluginutils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.endWith
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * The test verifies project properties. It is a direct rework of Spec tests, therefore test names are a little bit strange.
 *
 * The goal is to stop using Spec, because it is hard to run individual tests from IDE without additional plugins installation.
 */
class ProjectPropertiesTest {
    lateinit var project: Project

    @BeforeEach
    fun beforeEach() {
        project = ProjectBuilder.builder()
            .build()
    }

    @Test
    fun `providerFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "testValue")

        val provider = project.providerFromProjectProperty("testProperty")

        provider.orNull shouldBe "testValue"
    }

    @Test
    fun `providerFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.providerFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `providerFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.providerFromProjectProperty("testProperty", "defaultValue")

        provider.orNull shouldBe "defaultValue"
    }

    @Test
    fun `providerFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "testValue")

        val provider = project.providerFromProjectProperty("testProperty", "defaultValue")

        provider.orNull shouldBe "testValue"
    }

    @Test
    fun `providerFromProjectProperty_should evaluate GString`() {
        project.version = "1.2.3"

        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "ver\${version}")

        val provider = project.providerFromProjectProperty("testProperty", evaluateGString = true)

        provider.orNull shouldBe "ver1.2.3"
    }

    @Test
    fun `booleanProviderFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", true)

        val provider = project.booleanProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe true
    }

    @Test
    fun `booleanProviderFromProjectProperty_should use the value true if the project property is the string true`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "true")

        val provider = project.booleanProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe true
    }

    @Test
    fun `booleanProviderFromProjectProperty_should have value false if the project property is not true`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "someValue")

        val provider = project.booleanProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe false
    }

    @Test
    fun `booleanProviderFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.booleanProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `booleanProviderFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.booleanProviderFromProjectProperty("testProperty", true)

        provider.orNull shouldBe true
    }

    @Test
    fun `booleanProviderFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "false")

        val provider = project.booleanProviderFromProjectProperty("testProperty", true)

        provider.orNull shouldBe false
    }

    @Test
    fun `intProviderFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", 42)

        val provider = project.intProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe 42
    }

    @Test
    fun `intProviderFromProjectProperty_should convert a string value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "42")

        val provider = project.intProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe 42
    }

    @Test
    fun `intProviderFromProjectProperty_should throw an exception if the value cannot be converted to an integer`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "hello")

        val provider = project.intProviderFromProjectProperty("testProperty")

        shouldThrow<IllegalArgumentException> { provider.get() }
    }

    @Test
    fun `intProviderFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.intProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `intProviderFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.intProviderFromProjectProperty("testProperty", 123)

        provider.orNull shouldBe 123
    }

    @Test
    fun `intProviderFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "42")

        val provider = project.intProviderFromProjectProperty("testProperty", 123)

        provider.orNull shouldBe 42
    }

    @Test
    fun `dirProviderFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "/foo/bar")

        val provider = project.dirProviderFromProjectProperty("testProperty")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `dirProviderFromProjectProperty_should use the project dir as base for relative paths`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "foo/bar")

        val provider = project.dirProviderFromProjectProperty("testProperty")

        provider.asFile().orNull shouldBe project.projectDir.resolve("foo/bar")
    }

    @Test
    fun `dirProviderFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.dirProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `dirProviderFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.dirProviderFromProjectProperty("testProperty", "/default/path")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/default/path")
    }

    @Test
    fun `dirProviderFromProjectProperty_should use the project dir as base for relative path in default value`() {
        val provider = project.dirProviderFromProjectProperty("testProperty", "default/path")

        provider.asFile().orNull shouldBe project.projectDir.resolve("default/path")
    }

    @Test
    fun `dirProviderFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "/foo/bar")

        val provider = project.dirProviderFromProjectProperty("testProperty", "/default/path")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `dirProviderFromProjectProperty_should evaluate GString`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("fooDir", "/foo")
        extra.set("testProperty", "\${fooDir}/bar")

        val provider = project.dirProviderFromProjectProperty("testProperty", evaluateGString = true)

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `fileProviderFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "/foo/bar")

        val provider = project.fileProviderFromProjectProperty("testProperty")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `fileProviderFromProjectProperty_should use the project dir as base for relative paths`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "foo/bar")

        val provider = project.fileProviderFromProjectProperty("testProperty")

        provider.asFile().orNull shouldBe project.projectDir.resolve("foo/bar")
    }

    @Test
    fun `fileProviderFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.fileProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `fileProviderFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.fileProviderFromProjectProperty("testProperty", "/default/path")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/default/path")
    }

    @Test
    fun `fileProviderFromProjectProperty_should use the project dir as base for relative path in default value`() {
        val provider = project.fileProviderFromProjectProperty("testProperty", "default/path")

        provider.asFile().orNull shouldBe project.projectDir.resolve("default/path")
    }

    @Test
    fun `fileProviderFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "/foo/bar")

        val provider = project.fileProviderFromProjectProperty("testProperty", "/default/path")

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `fileProviderFromProjectProperty_should evaluate GString`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("fooDir", "/foo")
        extra.set("testProperty", "\${fooDir}/bar")

        val provider = project.fileProviderFromProjectProperty("testProperty", evaluateGString = true)

        provider.asFile().get().absolutePath.replace('\\', '/') should endWith("/foo/bar")
    }

    @Test
    fun `durationProviderFromProjectProperty_should use the value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", Duration.ofSeconds(42))

        val provider = project.durationProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe Duration.ofSeconds(42)
    }

    @Test
    fun `durationProviderFromProjectProperty_should convert an ISO string value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "PT3M30S")

        val provider = project.durationProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe Duration.ofSeconds(3 * 60 + 30)
    }

    @Test
    fun `durationProviderFromProjectProperty_should convert a duration string value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "3m30s")

        val provider = project.durationProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe Duration.ofSeconds(3 * 60 + 30)
    }

    @Test
    fun `durationProviderFromProjectProperty_should convert a number string value from the project property`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "42")

        val provider = project.durationProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe Duration.ofSeconds(42)
    }

    @Test
    fun `durationProviderFromProjectProperty_should not have a value if project property is not set`() {
        val provider = project.durationProviderFromProjectProperty("testProperty")

        provider.orNull shouldBe null
    }

    @Test
    fun `durationProviderFromProjectProperty_should use the default value if project property is not set`() {
        val provider = project.durationProviderFromProjectProperty("testProperty", Duration.ofSeconds(32))

        provider.orNull shouldBe Duration.ofSeconds(32)
    }

    @Test
    fun `durationProviderFromProjectProperty_should not use the default value if project property is set`() {
        val extra: ExtraPropertiesExtension = project.requiredExtension()
        extra.set("testProperty", "7m42s")

        val provider = project.durationProviderFromProjectProperty("testProperty", Duration.ofSeconds(32))

        provider.orNull shouldBe Duration.ofSeconds(7 * 60 + 42)
    }
}
