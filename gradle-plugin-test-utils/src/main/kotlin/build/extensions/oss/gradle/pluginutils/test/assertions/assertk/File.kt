package build.extensions.oss.gradle.pluginutils.test.assertions.assertk

import assertk.Assert
import assertk.assertions.isDirectory
import assertk.assertions.isFile
import java.io.File


fun Assert<File>.child(relativePath: String) = transform(name = "${this.name}/$relativePath") { actual ->
    actual.resolve(relativePath)
}


fun Assert<File>.childFile(relativePath: String) =
    child(relativePath).also { it.isFile() }


fun Assert<File>.childDir(relativePath: String) =
    child(relativePath).also { it.isDirectory() }
