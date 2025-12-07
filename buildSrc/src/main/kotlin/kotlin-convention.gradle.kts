import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("dependencies-lock")
    id("dependencies-publishing")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    // for Gradle dependencies
    gradlePluginPortal()
    // for all other jars
    mavenCentral()
}

configure<KotlinJvmProjectExtension> {
    // Set the toolchain 11 to do everything, because kotest doesn't support Java 8
    jvmToolchain(11)
}


tasks.withType<KotlinCompile>().configureEach {
    // manually decrease the version for releasing classes
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
tasks.withType<JavaCompile>().configureEach {
    // synchronize Java version with Kotlin compiler
    options.release.set(8)
}

dependencies {
    // compile only - we will use Gradle dependency in real life
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("stdlib-jdk8"))
}

kover {
    currentProject {
        sources {
            excludedSourceSets.add("test")
        }
    }
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

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$rootDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline = file("$rootDir/config/detekt-baseline.xml") // a way of suppressing issues before introducing detekt
}

val rootProjectBuildTask = rootProject.tasks.getByName("build")

tasks {
    // prohibit build without verification
    rootProjectBuildTask.dependsOn(getByName("koverCachedVerify"))
    // prohibit build without running detekt
    rootProjectBuildTask.dependsOn(getByName("detektMain"))
    rootProjectBuildTask.dependsOn(getByName("detektTest"))
}
