import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    `java-library`
    kotlin("jvm")
    id("dependencies-lock")
}


configure<KotlinJvmProjectExtension> {
    // fix the toolchain for now
    jvmToolchain(11)
}

dependencies {
    "compileOnly"(kotlin("stdlib"))
    "compileOnly"(kotlin("stdlib-jdk8"))
}