plugins {
    base
    // is defined in buildSrc
    id("org.jetbrains.kotlinx.kover")
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