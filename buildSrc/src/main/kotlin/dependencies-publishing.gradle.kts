import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

plugins {
    `maven-publish`
}

plugins.apply(com.vanniktech.maven.publish.MavenPublishPlugin::class)
plugins.apply(SigningPlugin::class)

val publishing = the<PublishingExtension>()

publishing.publications.withType<MavenPublication> {
    pom {
        val githubRepo = providers.gradleProperty("githubRepo")
        val githubUrl = githubRepo.map { "https://github.com/$it" }

        name.set(providers.gradleProperty("projectName"))
        description.set(providers.gradleProperty("projectDescription"))
        url.set(providers.gradleProperty("projectUrl"))
        licenses {
            license {
                name.set(providers.gradleProperty("projectLicenseName"))
                url.set(providers.gradleProperty("projectLicenseUrl"))
            }
        }
        developers {
            developer {
                name.set(providers.gradleProperty("developerName"))
                email.set(providers.gradleProperty("developerEmail"))
                url.set(providers.gradleProperty("developerUrl"))
            }
        }
        scm {
            url.set(githubUrl.map { "$it/tree/master" })
            connection.set(githubRepo.map { "scm:git:git://github.com/$it.git" })
            developerConnection.set(githubRepo.map { "scm:git:ssh://github.com:$it.git" })
        }
        issueManagement {
            url.set(githubUrl.map { "$it/issues" })
            system.set("GitHub")
        }
    }
}

publishing.repositories {
    maven {
        name = "local"
        url = file(layout.buildDirectory.dir("repos/releases")).toURI()
    }
}