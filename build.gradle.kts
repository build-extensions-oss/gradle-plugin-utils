plugins {
    base
    // is defined in buildSrc
    id("org.jetbrains.kotlinx.kover")
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
        // prohibit building without verification
        dependsOn(getByName("koverCachedVerify"))
    }
}

kover.reports {
    verify {
        rule {
            bound {
                minValue.set(14)
            }
        }
    }
}