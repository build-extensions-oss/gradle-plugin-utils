[![Codecov](https://codecov.io/gh/detekt/detekt/branch/main/graph/badge.svg)](https://codecov.io/gh/detekt/detekt)

Artefacts:
* gradle-plugin-utils: ![gradle-plugin-utils - Maven Central Version](https://img.shields.io/maven-central/v/io.github.build-extensions-oss/gradle-plugin-utils)
* gradle-plugin-test-utils: ![gradle-plugin-test-utils - Maven Central Version](https://img.shields.io/maven-central/v/io.github.build-extensions-oss/gradle-plugin-test-utils)
* gradle-plugin-integration-test-utils: ![gradle-plugin-integration-test-utils - Maven Central Version](https://img.shields.io/maven-central/v/io.github.build-extensions-oss/gradle-plugin-integration-test-utils)

This is the fork of [unbroken-dome/gradle-plugin-utils](https://github.com/unbroken-dome/gradle-plugin-utils) to support
newer Gradle.

The project has extensions to simplify Gradle plugins build and test.

# How to use

```kotlin
dependencies {
    implementation("io.github.build-extensions-oss", "gradle-plugin-utils", "latest")
    implementation("io.github.build-extensions-oss", "gradle-plugin-test-utils", "latest")
    implementation("io.github.build-extensions-oss", "gradle-plugin-integration-test-utils", "latest")
}
```

# Publishing

In short - publishing doesn't work well, because of breaking changes in Maven Central, plus Gradle Plugins incompatibilities.

At the time of writing Javadoc isn't published properly.

To run publishing tasks, the following needs to be done:

1. File `~/.gradle/gradle.properties` needs to be updated to:
```properties
mavenCentralUsername=???
mavenCentralPassword=???
```
1. And then the following task will work: `./gradlew publishToMavenCentral --stacktrace`

## Signing

Add lines like below into ~/.gradle/gradle.properties:
```properties
signing.keyId=F27AB2F4
signing.password=
# can be exported via gpg --export-secret-keys -o xxx.gpg
# see https://stackoverflow.com/a/39573795/185498
signing.secretKeyRingFile=xxx.gpg
```