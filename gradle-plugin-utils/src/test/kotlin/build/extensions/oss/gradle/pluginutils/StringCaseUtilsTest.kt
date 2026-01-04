package build.extensions.oss.gradle.pluginutils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class StringCaseUtilsTest {
    @Test
    fun `single word`() {
        val words = "word".splitIntoWords().toList()

        words shouldBe listOf("word")
    }

    @Test
    fun `two words camel case`() {
        val words = "twoWords".splitIntoWords().toList()

        words shouldBe listOf("two", "words")
    }

    @ParameterizedTest
    @ValueSource(chars = [' ', '-', '_', '.', '/'])
    fun `two words with separator`(separator: Char) {
        val expectedWords = listOf("two", "words")
        val concatenated = expectedWords.joinToString(separator = separator.toString())

        val words = concatenated.splitIntoWords().toList()

        words shouldBe expectedWords
    }
}
