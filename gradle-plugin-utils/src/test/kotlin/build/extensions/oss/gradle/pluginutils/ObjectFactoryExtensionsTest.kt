package build.extensions.oss.gradle.pluginutils

import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
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

        property should beInstanceOf<Property<*>>()
    }

    @Test
    fun listProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.listProperty<String>()

        property should beInstanceOf<ListProperty<*>>()
    }

    @Test
    fun setProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.setProperty<String>()

        property should beInstanceOf<SetProperty<*>>()
    }

    @Test
    fun mapProperty() {
        val project: Project = ProjectBuilder.builder()
            .build()

        val property = project.objects.mapProperty<String, String>()

        property should beInstanceOf<MapProperty<*, *>>()
    }
}
