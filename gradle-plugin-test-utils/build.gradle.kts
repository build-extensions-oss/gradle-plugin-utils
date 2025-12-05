plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}


dependencies {
    compileOnly(gradleApi())

    compileOnly(libs.junit.api)
    compileOnly(libs.spek.dsl)
    compileOnly(libs.assertk.core)

    implementation(project(":gradle-plugin-utils"))
    implementation(libs.reflections)
}
