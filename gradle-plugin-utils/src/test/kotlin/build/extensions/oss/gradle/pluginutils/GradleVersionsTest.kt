package build.extensions.oss.gradle.pluginutils

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.haveMessage
import io.mockk.mockk
import io.mockk.verify
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.full.memberProperties

class GradleVersionsTest {

    data class VersionInput(val major: Int, val minor: Int)

    companion object {
        val veryHighVersion = GradleVersion.version("9999.99")!!

        @JvmStatic
        fun versionsInput(): List<VersionInput> {
            return listOf(
                4 to 0..10,
                5 to 0..6,
                6 to 0..8
            ).flatMap { (majorVersion, minorRange) ->
                minorRange
                    .map { minorVersion ->
                        VersionInput(majorVersion, minorVersion)
                    }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("versionsInput")
    fun `GradleVersions object should have a version property`(input: VersionInput) {
        val allVersionProperties = GradleVersions::class.memberProperties.associateBy { it.name }

        val versionPropertyName = "Version_${input.major}_${input.minor}"
        val version = GradleVersion.version("${input.major}.${input.minor}")

        val versionProperty = allVersionProperties[versionPropertyName]

        versionProperty shouldNotBe null
        versionProperty!!.get(GradleVersions) shouldBe version
    }

    @Test
    fun `checkGradleVersion with message should return normally if the version is ok`() {
        shouldNotThrowAny {
            checkGradleVersion(GradleVersions.Version_6_2) {
                "version too low"
            }
        }
    }

    @Test
    fun `should throw an exception if the version is too low`() {
        val exception = shouldThrow<IllegalStateException> {
            checkGradleVersion(veryHighVersion) { "version too low" }
        }

        exception should haveMessage("version too low")
    }

    @Test
    fun `checkGradleVersion with plugin ID should return normally if the version is ok`() {
        shouldNotThrowAny {
            checkGradleVersion(GradleVersions.Version_6_2, "test.plugin")
        }
    }

    @Test
    fun `checkGradleVersion with plugin ID should throw an exception if the version is too low`() {

        val exception = shouldThrow<IllegalStateException> {
            checkGradleVersion(veryHighVersion, "test.plugin")
        }

        exception should haveMessage("The plugin \"test.plugin\" requires at least Gradle 9999.99")
    }

    @Test
    fun `withMinGradleVersion should execute the block if the version is ok`() {
        val block = mockk<Runnable>(relaxed = true)

        withMinGradleVersion(GradleVersions.Version_6_2, block::run)

        verify(exactly = 1) { block.run() }
    }

    @Test
    fun `withMinGradleVersion should not execute the block if the version is too low`() {
        val block = mockk<Runnable>(relaxed = true)

        withMinGradleVersion(veryHighVersion, block::run)

        verify(exactly = 0) { block.run() }
    }

    @Test
    fun `withMinGradleVersion with fallback should execute the block if the version is ok`() {
        val block = mockk<Runnable>("block", relaxed = true)
        val fallback = mockk<Runnable>("fallback", relaxed = true)

        withMinGradleVersion(GradleVersions.Version_6_2, block::run, fallback::run)

        verify(exactly = 1) { block.run() }
        verify(exactly = 0) { fallback.run() }
    }

    @Test
    fun `withMinGradleVersion with fallback should execute the fallback if the version is too low`() {
        val block = mockk<Runnable>("block", relaxed = true)
        val fallback = mockk<Runnable>("fallback", relaxed = true)

        withMinGradleVersion(veryHighVersion, block::run, fallback::run)

        verify(exactly = 0) { block.run() }
        verify(exactly = 1) { fallback.run() }
    }
}