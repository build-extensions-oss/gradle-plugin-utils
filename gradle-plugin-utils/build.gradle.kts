plugins {
    kotlin("jvm")
    id("kotlin-convention") // keep shared logic here
    `java-library`
    `maven-publish`
}


dependencies {
    compileOnly(gradleApi())

    "testImplementation"(gradleApi())
    "testImplementation"(project(":gradle-plugin-test-utils"))
    "testImplementation"(libs.kotest.runner)
    "testImplementation"(libs.kotest.property)
    "testImplementation"(libs.assertk.core)
    "testImplementation"(libs.mockk)
}
