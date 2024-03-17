package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.json.psi.JsonElement
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.settings.SolarProjectConfiguration
import com.solanteq.solar.plugin.util.isForm
import javax.swing.JPanel

//TODO work in progress
class L10nInlayProvider : InlayHintsProvider<NoSettings> {

    override val key = SettingsKey<NoSettings>("solar.l10n.inlayHint")

    override val name = "Localization string"

    override val previewText = "TODO"

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) : InlayHintsCollector? {
        if(file.isForm()) return Collector
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
            val localizationValue = formElement.getL10nValue(locale) ?: return
            val valueElement = formElement.namePropertyValue ?: return
            val offset = valueElement.textRange.endOffset

            val factory = PresentationFactory(editor)
            val presentation = factory.smallText(localizationValue)
            sink.addInlineElement(offset, false, presentation, true)
        }

    }

}