import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.net.URI

plugins {
    kotlin("jvm") apply false
    alias(libs.plugins.com.vanniktech.maven.publish) apply false
}

subprojects {

    plugins.withId("org.jetbrains.kotlin.jvm") {

        configure<KotlinJvmProjectExtension> {
            jvmToolchain(11)
        }

        dependencies {
            "compileOnly"(kotlin("stdlib"))
            "compileOnly"(kotlin("stdlib-jdk8"))
        }
    }

    plugins.withType<JavaPlugin> {

        configure<JavaPluginExtension> {
            withSourcesJar()
            // We don't publish Javadoc, because it is useless in out case. We publish sources, plus it is easy to find
            // GitHub project and read the source code. We don't have any comprehensive documentation, so let's just publish nothing.
            // Additionally, JavaDoc task conflicts with publishing plugin, so let's simply delete one of them.
            // withJavadocJar()
        }

        tasks.withType<Test> {
            useJUnitPlatform()
            systemProperty("java.io.tmpdir", layout.buildDirectory.dir("tmp"))
        }

        plugins.withType<MavenPublishPlugin> {
            plugins.apply(com.vanniktech.maven.publish.MavenPublishPlugin::class)

            with(the<PublishingExtension>()) {
                publications.create<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
        }
    }


    plugins.withType<MavenPublishPlugin> {
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
    }
}

