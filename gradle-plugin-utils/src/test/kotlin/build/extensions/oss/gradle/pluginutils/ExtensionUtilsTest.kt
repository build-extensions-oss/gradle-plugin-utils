package build.extensions.oss.gradle.pluginutils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.string.haveSubstring
import io.kotest.matchers.types.beTheSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.junit.jupiter.api.Test

class ExtensionUtilsTest {

    /**
     * Ensure we don't do strict cast internally
     */
    @Test
    fun shouldReturnNullForNonExtensionAware() {
        val expectedMessages = listOf(
            "org.gradle.api.plugins.ExtensionAware",
            "cannot be cast to"
        )
        val somethingWhichIsNotExtension = "131223"

        val exception = shouldThrow<Exception> { somethingWhichIsNotExtension.requiredExtension<String>("abc") }

        expectedMessages.forEach { message ->
            exception.message should haveSubstring(message)
        }
    }

    /**
     * Check if we propagate the call to API below.
     * Basically, we ensure that our intermediate code doesn't have something new.
     */
    @Test
    fun shouldPropagateLookupCall() {
        val extensionName = "abc"
        // some object which will play a role of extension
        val extensionToResolve = Runnable {}

        val extensionsContainer = mockk<ExtensionContainer> {
            every { getByName(extensionName) } returns extensionToResolve
        }
        val extensionAware = mockk<ExtensionAware> {
            every { extensions } returns extensionsContainer
        }

        val exception = extensionAware.requiredExtension<Runnable>(extensionName)

        exception should beTheSameInstanceAs(extensionToResolve)
    }
}
