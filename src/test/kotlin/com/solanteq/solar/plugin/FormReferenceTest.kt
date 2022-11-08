package com.solanteq.solar.plugin

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class FormReferenceTest : LightJavaCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testData"
    }

    fun test() {
        myFixture.copyTestBundle("reference", "serviceNameReference")
        myFixture.openBundleFile("testForm.json")

        myFixture.getReferenceAtCaretPositionWithAssertion()
    }

}