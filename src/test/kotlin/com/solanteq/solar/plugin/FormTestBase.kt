package com.solanteq.solar.plugin

import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testFramework.fixtures.MavenDependencyUtil

abstract class FormTestBase : LightJavaCodeInsightFixtureTestCase5(DEFAULT_DESCRIPTOR) {

    final override fun getTestDataPath() = "src/test/testData/${getTestDataSuffix()}"

    open fun getTestDataSuffix() = ""

    companion object {

        private val DEFAULT_DESCRIPTOR = object : DefaultLightProjectDescriptor() {

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

    }

}