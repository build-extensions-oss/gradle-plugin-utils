package build.extensions.oss.gradle.pluginutils

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.of


class StringCaseUtilsTest : StringSpec({

    "single word" {

        val words = "word".splitIntoWords().toList()

        assertThat(words)
            .containsExactly("word")
    }

    "two words camel case" {

        val words = "twoWords".splitIntoWords().toList()

        assertThat(words)
            .containsExactly("two", "words")
    }

    "two words with separator" {

        val separators = Exhaustive.of(' ', '-', '_', '.', '/')

        checkAll(separators) { separator ->
            val expectedWords = listOf("two", "words")
            val concatenated = expectedWords.joinToString(separator = separator.toString())

            val words = concatenated.splitIntoWords().toList()

            assertThat(words).isEqualTo(expectedWords)
        }
    }
})
