package com.solanteq.solar.plugin.base

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testFramework.junit5.RunInEdt

/**
 * JUnit5 tests runner base class for tests that require interaction with java code.
 *
 * Adds `spring` and `SOLAR Platform` dependencies.
 *
 * Tests pass much slower than in [LightPluginTestBase], so consider using it only when
 * java code interaction is necessary.
 */
@RunInEdt(writeIntent = true)
abstract class JavaPluginTestBase : LightJavaCodeInsightFixtureTestCase5(SolarJavaProjectDescriptor) {

    open fun getTestDataSuffix() = ""

    override fun getTestDataPath() = "src/test/testData/${getTestDataSuffix()}"

}