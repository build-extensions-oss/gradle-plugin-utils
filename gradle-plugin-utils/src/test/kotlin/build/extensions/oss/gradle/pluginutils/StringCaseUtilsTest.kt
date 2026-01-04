package build.extensions.oss.gradle.pluginutils

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class StringCaseUtilsTest {
    @Test
    fun `single word`() {
        val words = "word".splitIntoWords().toList()

        assertThat(words)
            .containsExactly("word")
    }

    @Test
    fun `two words camel case`() {
        val words = "twoWords".splitIntoWords().toList()

        assertThat(words)
            .containsExactly("two", "words")
    }

    @ParameterizedTest
    @ValueSource(chars = [' ', '-', '_', '.', '/'])
    fun `two words with separator`(separator: Char) {
        val expectedWords = listOf("two", "words")
        val concatenated = expectedWords.joinToString(separator = separator.toString())

        val words = concatenated.splitIntoWords().toList()

        assertThat(words).isEqualTo(expectedWords)
    }
}
