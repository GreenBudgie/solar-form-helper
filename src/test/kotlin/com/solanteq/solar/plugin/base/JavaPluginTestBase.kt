package com.solanteq.solar.plugin.base

import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.*
import com.solanteq.solar.plugin.util.asList
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * JUnit5 tests runner base class for tests that require interaction with java code.
 *
 * Adds `spring` and `SOLAR Platform` dependencies.
 *
 * Tests pass much slower than in [LightPluginTestBase], so consider using it only when
 * java code interaction is necessary.
 */
abstract class JavaPluginTestBase : PluginTestBase() {

    override val fixture: JavaCodeInsightTestFixture get() = testCase.fixture

    @RegisterExtension
    @Suppress("JUnitMalformedDeclaration")
    private val testCase = object : LightJavaCodeInsightFixtureTestCase(), BeforeEachCallback, AfterEachCallback {

        val fixture: JavaCodeInsightTestFixture get() = myFixture

        override fun getProjectDescriptor() = SOLAR_DESCRIPTOR

        override fun getTestDataPath() = baseTestDataPath

        override fun beforeEach(context: ExtensionContext) = setUp()

        override fun afterEach(context: ExtensionContext) = tearDown()

    }

    companion object {

        private val SOLAR_DESCRIPTOR = object : DefaultLightProjectDescriptor() {

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
                    RemoteRepositoryDescription(
                        "solar",
                        "SOLAR Repository",
                        "https://karjala.solanteq.com/content/repositories/releases/"
                    ).asList()
                )
            }

            override fun getSdk() = IdeaTestUtil.getMockJdk18()

        }

    }

}