package com.solanteq.solar.plugin.base

import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import com.solanteq.solar.plugin.util.asList

/**
 * Describes a SOLAR project with spring and solar-commons dependencies. Downloads them automatically from repo.
 *
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class SolarJavaProjectDescriptor(
    private val dependencies: Set<SolarDependency>
) : DefaultLightProjectDescriptor() {

    override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
        withSolarDependency(model, "com.solanteq.solar:solar-commons:$PLATFORM_VERSION")

        dependencies.forEach {
            withSolarDependency(model, it.mavenCoordinates)
        }

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