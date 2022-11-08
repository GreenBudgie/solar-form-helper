package com.solanteq.solar.plugin

import com.intellij.openapi.module.ResourceFileUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import java.io.File

/**
 * **Test bundle** is a testData directory containing mainly forms and services.
 * Any bundle may have sub bundles.
 *
 * This util provides an easy way of copying bundles or separate files from them into test project.
 *
 * The bundle structure must look like the following:
 * - Forms location: `<bundle name>/<optional sub bundle name>/forms/<optional group name>`
 * - Included forms location: `<bundle name>/<optional sub bundle name>/includedForms/<optional group name>`
 * - Services (interfaces) location: `<bundle name>/<optional sub bundle name>/services/`
 * - Services (implementations) location: `<bundle name>/<optional sub bundle name>/services/impl`
 */

/**
 * Copies the entire test bundle into the project
 */
fun JavaCodeInsightTestFixture.copyTestBundle(bundleName: String, subBundleName: String = "") {
    TestBundleResource.values().forEach { copyTestBundleResource(it, bundleName, subBundleName) }
}

/**
 * Copies a specific test bundle resource into the project
 */
fun JavaCodeInsightTestFixture.copyTestBundleResource(
    resource: TestBundleResource,
    bundleName: String,
    subBundleName: String = ""
) {
    if(!File("$testDataPath/${resource.bundleDirectory}").isDirectory) {
        return
    }

    val bundleFullPath = "$bundleName/$subBundleName/"
    copyDirectoryToProject(
        "$bundleFullPath${resource.bundleDirectory}",
        resource.realDirectory
    )
}

/**
 * Opens a bundle file with provided name in editor.
 * If multiple files are found opens the first one.
 */
fun JavaCodeInsightTestFixture.openBundleFile(fileName: String) {
    val file = ResourceFileUtil.findResourceFileInProject(project, fileName) ?:
        error("File $fileName is not found in bundle")
    openFileInEditor(file)
}

enum class TestBundleResource(
    val bundleDirectory: String,
    val realDirectory: String
) {

    FORMS("forms", "src/main/resources/config/forms"),
    INCLUDED_FORMS("includedForms", "src/main/resources/config/includes/forms"),
    SERVICES("services", "src/main/kotlin/com/solanteq/solar/test/service")

}