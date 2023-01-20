package com.solanteq.solar.plugin.base

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * JUnit5 tests runner base class for simple tests that do not require interaction with java code
 */
abstract class LightPluginTestBase : PluginTestBase() {

    override val fixture: CodeInsightTestFixture get() = testCase.fixture

    @RegisterExtension
    @Suppress("JUnitMalformedDeclaration")
    private val testCase = object : BasePlatformTestCase(), BeforeEachCallback, AfterEachCallback {

        val fixture: CodeInsightTestFixture get() = myFixture

        override fun getProjectDescriptor() = LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR

        override fun getTestDataPath() = baseTestDataPath

        override fun beforeEach(context: ExtensionContext?) = setUp()

        override fun afterEach(context: ExtensionContext?) = tearDown()

    }

}