package org.unbrokendome.gradle.pluginutils

import assertk.assertThat
import assertk.assertions.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.Headers2
import io.kotest.data.Row2
import io.kotest.data.Table2
import io.kotest.data.forAll
import io.mockk.mockk
import io.mockk.verify
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.reflect.full.memberProperties

class GradleVersionsTest {

    data class VersionInput(val major: Int, val minor: Int)

    companion object {
        @JvmStatic
        fun versionsInput() : List<VersionInput> {
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

        assertThat(versionProperty, name = "Version property").isNotNull()
            .transform { it.get(GradleVersions) }
            .isEqualTo(version)
    }
}


class GradleVersionsTestOld : DescribeSpec({
    val veryHighVersion = GradleVersion.version("9999.99")


    describe("checkGradleVersion with message") {

        it("should return normally if the version is ok") {
            assertThat {
                checkGradleVersion(GradleVersions.Version_6_2) { "version too low" }
            }.isSuccess()
        }

        it("should throw an exception if the version is too low") {
            assertThat {
                checkGradleVersion(veryHighVersion) { "version too low" }
            }.isFailure()
                .isInstanceOf(IllegalStateException::class)
                .hasMessage("version too low")
        }
    }


    describe("checkGradleVersion with plugin ID") {

        it("should return normally if the version is ok") {
            assertThat {
                checkGradleVersion(GradleVersions.Version_6_2, "test.plugin")
            }.isSuccess()
        }

        it("should throw an exception if the version is too low") {
            assertThat {
                checkGradleVersion(veryHighVersion, "test.plugin")
            }.isFailure()
                .isInstanceOf(IllegalStateException::class)
                .hasMessage("The plugin \"test.plugin\" requires at least Gradle 9999.99")
        }
    }


    describe("withMinGradleVersion") {

        it("should execute the block if the version is ok") {

            val block = mockk<Runnable>(relaxed = true)

            withMinGradleVersion(GradleVersions.Version_6_2, block::run)

            verify(exactly = 1) { block.run() }
        }


        it("should not execute the block if the version is too low") {

            val block = mockk<Runnable>(relaxed = true)

            withMinGradleVersion(veryHighVersion, block::run)

            verify(exactly = 0) { block.run() }
        }
    }


    describe("withMinGradleVersion with fallback") {

        it("should execute the block if the version is ok") {

            val block = mockk<Runnable>("block", relaxed = true)
            val fallback = mockk<Runnable>("fallback", relaxed = true)

            withMinGradleVersion(GradleVersions.Version_6_2, block::run, fallback::run)

            verify(exactly = 1) { block.run() }
            verify(exactly = 0) { fallback.run() }
        }

        it("should execute the fallback if the version is too low") {

            val block = mockk<Runnable>("block", relaxed = true)
            val fallback = mockk<Runnable>("fallback", relaxed = true)

            withMinGradleVersion(veryHighVersion, block::run, fallback::run)

            verify(exactly = 0) { block.run() }
            verify(exactly = 1) { fallback.run() }
        }
    }
})
