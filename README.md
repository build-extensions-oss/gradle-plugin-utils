This is the fork of [unbroken-dome/gradle-plugin-utils](https://github.com/unbroken-dome/gradle-plugin-utils) to support
newer Gradle.

The project has extensions to simplify Gradle plugins build and test.

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