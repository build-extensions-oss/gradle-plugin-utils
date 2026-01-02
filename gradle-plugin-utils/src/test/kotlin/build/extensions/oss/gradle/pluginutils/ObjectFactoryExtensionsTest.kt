package build.extensions.oss.gradle.pluginutils

import assertk.assertThat
import assertk.assertions.isInstanceOf
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class ObjectFactoryExtensionsTest {
    @Test
    fun property() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.property<String>()

        assertThat(property, "property")
            .isInstanceOf(Property::class.java)
    }

    @Test
    fun listProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.listProperty<String>()

        assertThat(property, "property")
            .isInstanceOf(ListProperty::class.java)
    }

    @Test
    fun setProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.setProperty<String>()

        assertThat(property, "property")
            .isInstanceOf(SetProperty::class.java)
    }

    @Test
    fun mapProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.mapProperty<String, String>()

        assertThat(property, "property")
            .isInstanceOf(MapProperty::class.java)
    }
}
