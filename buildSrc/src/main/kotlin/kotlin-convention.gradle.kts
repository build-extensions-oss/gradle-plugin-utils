import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    `java-library`
    kotlin("jvm")
    id("dependencies-lock")
    id("dependencies-publishing")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    // for Gradle dependencies
    gradlePluginPortal()
    // for all other jars
    mavenCentral()
}

configure<KotlinJvmProjectExtension> {
    // Use the oldest toolchain possible
    // if Gradle complains - download the JDK. If Gradle can't find it - use property 'org.gradle.java.installations.paths'
    jvmToolchain(8)
}

dependencies {
    // compile only - we will use Gradle dependency in real life
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("stdlib-jdk8"))
}


configure<JavaPluginExtension> {
    withSourcesJar()
    // We don't publish Javadoc, because it is useless in out case. We publish sources, plus it is easy to find
    // GitHub project and read the source code. We don't have any comprehensive documentation, so let's just publish nothing.
    // Additionally, JavaDoc task conflicts with publishing plugin, so let's simply delete one of them.
    // withJavadocJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    // store all temporary results inside the Gradle folder
    val localTempFolder = layout.buildDirectory.dir("tmp").get().asFile
    systemProperty("java.io.tmpdir", localTempFolder.absolutePath)

    doFirst {
        // create the folder if needed
        localTempFolder.mkdirs()
    }
}