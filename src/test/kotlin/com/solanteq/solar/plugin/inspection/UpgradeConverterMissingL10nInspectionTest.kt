package com.solanteq.solar.plugin.inspection

import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.SolarDependency
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.createL10nFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class UpgradeConverterMissingL10nInspectionTest : JavaPluginTestBase(
    SolarDependency.JOBS_UPGRADE_CONVERTER,
    SolarDependency.HIBERNATE_COMMONS,
) {

    override fun getTestDataSuffix() = "inspection/converter"

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test converter l10n highlighting - kotlin, no abstraction`(includeL10n: Boolean) =
        converterL10nInspectionTest {
            files("TestEntity.kt")
            kotlin()
            includeL10n(includeL10n)
            converter {
                """
            import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter
            
            class $className : AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

                override val module = "$MODULE_NAME"
                    
                override val versionTo = "$VERSION_TO"

            }
            """.trimIndent()
            }
        }

    @Test
    fun `test present l10n - kotlin, no abstraction`(): Unit = with(fixture) {
        configureByFiles("TestEntity.kt")

        configureByText(
            "TestConverter.kt", """
            import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter
            
            class <warning>TestConverter</warning> : 
                AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

                override val module = "test"
                    
                override val versionTo = "1.0.0"

            }
        """.trimIndent()
        )

        enableInspections(UpgradeConverterMissingL10nInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test missing l10n - kotlin, no abstraction, with getter`(): Unit = with(fixture) {
        configureByFiles("TestEntity.kt")

        configureByText(
            "TestConverter.kt", """
            import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter
            
            class <warning>TestConverter</warning> : 
                AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

                override val module
                    get() = "test"
                    
                override val versionTo = "1.0.0"

            }
        """.trimIndent()
        )

        enableInspections(UpgradeConverterMissingL10nInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test missing l10n - kotlin, no abstraction, with overloaded table name`(): Unit = with(fixture) {
        configureByFiles("TestEntity.kt")

        configureByText(
            "TestConverter.kt", """
            import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter
            
            class <warning>TestConverter</warning> : 
                AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

                override val module = "test"
                
                override val tableName = "test_table"
                    
                override val versionTo = "1.0.0"

            }
        """.trimIndent()
        )
    }

    private fun converterL10nInspectionTest(
        configuration: ConverterL10nInspectionTest.() -> Unit,
    ) {
        val test = ConverterL10nInspectionTest()
        test.configuration()
        test.runTest()
    }

    private inner class ConverterL10nInspectionTest {

        private var files: Set<String> = emptySet()
        private var converterTextBuilder: (TextBuilder.() -> String)? = null
        private var includeL10n: Boolean = true
        private var language: Language = Language.JAVA

        fun files(vararg files: String) {
            this.files = files.toSet()
        }

        fun converter(textBuilder: TextBuilder.() -> String) {
            this.converterTextBuilder = textBuilder
        }

        fun includeL10n(includeL10n: Boolean) {
            this.includeL10n = includeL10n
        }

        fun kotlin() {
            language = Language.KOTLIN
        }

        fun runTest() = with(fixture) {
            val converterTextBuilder = converterTextBuilder
                ?: throw IllegalStateException("Converter text is not provided")

            if (files.isNotEmpty()) {
                configureByFiles(*files.toTypedArray())
            }

            generateL10ns()

            val textBuilder = TextBuilder(includeL10n)
            configureByText("$CONVERTER_NAME.${language.extension}", textBuilder.converterTextBuilder())

            enableInspections(UpgradeConverterMissingL10nInspection::class.java)
            checkHighlighting()
        }

        private fun generateL10ns() {
            when (includeL10n) {
                true -> L10nLocale.entries.forEach {
                    fixture.createL10nFile("l10n", it, L10N_KEY to "value")
                }
                false -> return
            }
        }

    }

    private inner class TextBuilder(val includeL10n: Boolean) {

        val className
            get() = when (includeL10n) {
                true -> CONVERTER_NAME
                false -> "<warning>$CONVERTER_NAME</warning>"
            }

    }

    private enum class Language(val extension: String) {

        JAVA("java"), KOTLIN("kt")

    }

    private companion object {

        const val CONVERTER_NAME = "TestConverter"
        const val TABLE_NAME = "test_table"
        const val MODULE_NAME = "test"
        const val VERSION_TO = "1.0.0"
        const val L10N_KEY = "$MODULE_NAME.upg.$TABLE_NAME.1_0_0.job_name"

    }

}