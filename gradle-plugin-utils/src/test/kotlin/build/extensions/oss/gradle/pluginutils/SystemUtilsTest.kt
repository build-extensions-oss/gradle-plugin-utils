package build.extensions.oss.gradle.pluginutils

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

/**
 * Test if we return more or less adequate values. Basically, we can't verify that without running integration tests,
 * therefore let's check that at least some values are more or less good here.
 *
 * We can't check (for example) it easily on ARM on GitHub.
 *
 * We run the same test on different operating systems,
 * so we will never run all methods from this class at the same test run.
 */
class SystemUtilsTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun testWindowsClassifier() {
        SystemUtils.getOperatingSystemClassifier() shouldBe "windows-amd64"
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun testLinuxClassifier() {
        SystemUtils.getOperatingSystemClassifier() should startWith("linux-")
    }

    @Test
    @EnabledOnOs(OS.MAC)
    fun testMacClassifier() {
        SystemUtils.getOperatingSystemClassifier() should startWith("darwin-")
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun testWindowsArchiveFormat() {
        SystemUtils.getOperatingSystemArchiveFormat() shouldBe "zip"
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun testPosixArchiveFormat() {
        SystemUtils.getOperatingSystemArchiveFormat() shouldBe "tar.gz"
    }
}
