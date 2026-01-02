plugins {
    id("kotlin-convention") // keep shared logic here
}


dependencies {

    api(project(":gradle-plugin-test-utils"))

    compileOnly(gradleApi())
    compileOnly(gradleTestKit())

    compileOnly(libs.junit.api)
    compileOnly(libs.kotest.api)
    compileOnly(libs.spek.dsl)
    compileOnly(libs.assertk.core)
}
