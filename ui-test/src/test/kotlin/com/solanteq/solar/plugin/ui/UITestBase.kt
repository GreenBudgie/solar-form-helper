package com.solanteq.solar.plugin.ui

import org.junit.jupiter.api.BeforeAll

open class UITestBase {

    companion object {

        @JvmStatic
        @BeforeAll
        fun attachUITestManager() {
            UITestManager.initializeIfNeeded()
        }

    }

}