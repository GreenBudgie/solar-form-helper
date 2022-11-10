package com.solanteq.solar.plugin

import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.psi.PsiFile
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import com.solanteq.solar.plugin.file.FormFileType

internal val DEFAULT_DESCRIPTOR = object : DefaultLightProjectDescriptor() {

    override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
        MavenDependencyUtil.addFromMaven(model, "org.springframework:spring-context:5.3.23")
        withSolarDependency(model, "com.solanteq.solar:solar-commons:3.3.5.1.RELEASE")
        super.configureModule(module, model, contentEntry)
    }

    private fun withSolarDependency(model: ModifiableRootModel, dependency: String) {
        MavenDependencyUtil.addFromMaven(
            model,
            dependency,
            true,
            DependencyScope.COMPILE,
            listOf(
                RemoteRepositoryDescription(
                    "solar",
                    "SOLAR Repository",
                    "https://karjala.solanteq.com/content/repositories/releases/"
                )
            )
        )
    }

    override fun getSdk() = IdeaTestUtil.getMockJdk18()

}

fun testDataPathWithSuffix(suffix: String) = "src/test/testData/$suffix/"

/**
 * Copies forms form testData directory to the proper location and opens the first form in editor
 */
fun JavaCodeInsightTestFixture.configureByForms(vararg formPaths: String): PsiFile? {
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$it")
    }
    virtualFiles.firstOrNull()?.let { configureFromExistingVirtualFile(it) }
    return file
}

fun JavaCodeInsightTestFixture.configureByFormText(formText: String): PsiFile = this.configureByText(FormFileType, formText)