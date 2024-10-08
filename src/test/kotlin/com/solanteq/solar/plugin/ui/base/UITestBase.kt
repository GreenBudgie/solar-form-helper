package com.solanteq.solar.plugin.ui.base

import org.junit.jupiter.api.BeforeAll

open class UITestBase {

    companion object {

        @JvmStatic
        @BeforeAll
        fun initializeUITestManager() {
            UITestManager.initializeIfNeeded()
        }

    }

}