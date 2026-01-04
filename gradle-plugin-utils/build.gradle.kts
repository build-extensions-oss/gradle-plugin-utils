plugins {
    id("kotlin-convention") // keep shared logic here
}


dependencies {
    compileOnly(gradleApi())

    testImplementation(gradleApi())

    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.property)
    testImplementation(libs.assertk.core)
    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junitEngine)
}

/**
 * See https://docs.gradle.org/current/userguide/upgrading_version_7.html#remove_test_add_opens .
 *
 * These options are added by default for Gradle plugin projects, however we have to mention them manually to support Java 17.
 */
tasks.withType(Test::class.java).configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}
