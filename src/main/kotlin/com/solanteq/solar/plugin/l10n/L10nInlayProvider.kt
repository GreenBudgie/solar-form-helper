package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.json.psi.JsonElement
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.settings.SolarProjectConfiguration
import com.solanteq.solar.plugin.util.isForm
import javax.swing.JPanel

/**
 * Provides hits for l10n values at the end of the line for localizable form elements
 */
class L10nInlayProvider : InlayHintsProvider<NoSettings> {

    override val key = SettingsKey<NoSettings>("solar.l10n.inlayHint")

    override val name = SolarBundle.message("l10.inlay.provider.name")

    override val previewText = null

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) : InlayHintsCollector? {
        if(file.isForm()) {
            return Collector
        }
        return null
    }

    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {

        override fun createComponent(listener: ChangeListener) = JPanel()

    }

    private object Collector : InlayHintsCollector {

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if(element !is JsonElement) return true
            val formElement = FormElementFactory.createLocalizableElement(element) ?: return true
            addHint(formElement, editor, sink)
            return true
        }

        private fun addHint(formElement: FormLocalizableElement<*>,
                            editor: Editor,
                            sink: InlayHintsSink) {
            if(editor !is EditorImpl) {
                return
            }
            val locale = service<SolarProjectConfiguration>().state.locale
            val distinctL10nValues = formElement.getL10nValues(locale).distinct()
            if (distinctL10nValues.isEmpty()) {
                return
            }
            val l10nNumber = distinctL10nValues.size
            val l10nNumberInfo = if (l10nNumber == 1) {
                ""
            } else {
                " +${l10nNumber - 1}"
            }
            val l10nValue = distinctL10nValues.first()
            val l10nInfo = l10nValue + l10nNumberInfo
            val valueElement = formElement.namePropertyValue ?: return
            val offset = valueElement.textRange.endOffset

            val factory = PresentationFactory(editor)
            val presentation = factory.roundWithBackground(factory.smallText(l10nInfo))
            sink.addInlineElement(offset, false, presentation, true)
        }

    }

}