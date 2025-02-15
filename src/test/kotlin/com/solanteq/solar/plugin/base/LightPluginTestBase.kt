package com.solanteq.solar.plugin.base

import com.intellij.testFramework.LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testFramework.junit5.RunInEdt

/**
 * JUnit5 tests runner base class for simple tests that do not require interaction with java code
 */
@RunInEdt(writeIntent = true)
abstract class LightPluginTestBase : LightJavaCodeInsightFixtureTestCase5(EMPTY_PROJECT_DESCRIPTOR) {

    open fun getTestDataSuffix() = ""

    override fun getTestDataPath() = "src/test/testData/${getTestDataSuffix()}"

}