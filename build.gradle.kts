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

kover.reports {
    verify {
        rule {
            bound {
                minValue.set(50)
                maxValue.set(75)
            }
        }
    }
}