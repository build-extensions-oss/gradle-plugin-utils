pluginManagement {

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy.eachPlugin {
        if (requested.id.namespace == "org.jetbrains.kotlin" ||
                requested.id.namespace.orEmpty().startsWith("org.jetbrains.kotlin.")) {
            useVersion(embeddedKotlinVersion)
        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-plugin-utils-parent"

include(
    "bom",
    "gradle-plugin-integration-test-utils",
    "gradle-plugin-test-utils",
    "gradle-plugin-utils"
)
