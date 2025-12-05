plugins {
    base
    alias(libs.plugins.org.jetbrains.kotlinx.kover)
}

dependencies {
    subprojects.forEach {
        val projectName = it.name

        // depend on all projects
        if (projectName != "bom") {
            kover(project(":${it.name}")) {}
        }
    }
}

tasks {
    this.getByName("build") {
        dependsOn(getByName("koverXmlReport"))
    }
}