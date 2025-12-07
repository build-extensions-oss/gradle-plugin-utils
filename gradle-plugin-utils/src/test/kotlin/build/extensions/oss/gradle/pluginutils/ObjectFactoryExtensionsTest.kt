package build.extensions.oss.gradle.pluginutils

import assertk.assertThat
import assertk.assertions.isInstanceOf
import io.kotest.core.spec.style.StringSpec
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.testfixtures.ProjectBuilder


@Suppress("UnstableApiUsage")
class ObjectFactoryExtensionsTest : StringSpec({

    val project: Project = ProjectBuilder.builder()
        .build()


    "property" {

        val property = project.objects.property<String>()

        assertThat(property, "property")
            .isInstanceOf(Property::class.java)
    }


    "listProperty" {

        val property = project.objects.listProperty<String>()

        assertThat(property, "property")
            .isInstanceOf(ListProperty::class.java)
    }


    "setProperty" {

        val property = project.objects.setProperty<String>()

        assertThat(property, "property")
            .isInstanceOf(SetProperty::class.java)
    }


    "mapProperty" {

        val property = project.objects.mapProperty<String, String>()

        assertThat(property, "property")
            .isInstanceOf(MapProperty::class.java)
    }
})
