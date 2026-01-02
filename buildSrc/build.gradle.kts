plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
    implementation(libs.com.vanniktech.maven.publish.gradle.plugin)
    implementation(libs.org.jetbrains.kotlinx.kover)
    implementation(libs.io.gitlab.arturbosch.detekt.detekt.gradle.plugin)
    implementation(libs.org.jetbrains.kotlinx.binary.compatibility.validator.gradle.plugin)
}