package com.solanteq.solar.plugin.l10n

import com.github.weisj.jsvg.T
import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementCreator
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
            when(element) {
                is JsonObject -> {
                    if(tryAddHint(element, editor, sink, FormField)) return true
                    if(tryAddHint(element, editor, sink, FormGroup)) return true
                }
                is JsonFile -> {
                    if(tryAddHint(element, editor, sink, FormRootFile)) return true
                }
            }
            return true
        }

        private fun <T : JsonElement> tryAddHint(
            element: T,
            editor: Editor,
            sink: InlayHintsSink,
            elementCreator: FormElementCreator<FormLocalizableElement<T>, T>
        ): Boolean {
            elementCreator.createFrom(element)?.let {
                addHint(it, editor, sink)
                return true
            }
            return false
        }

        private fun addHint(formElement: FormLocalizableElement<*>,
                            editor: Editor,
                            sink: InlayHintsSink) {
            if(editor !is EditorImpl) {
                return
            }
            //TODO add ru/en selection support
            val localizationValue = formElement.getL10nValues(L10nLocale.RU).firstOrNull() ?: return
            val valueElement = formElement.namePropertyValue ?: return
            val offset = valueElement.textRange.endOffset

            val factory = PresentationFactory(editor)
            val presentation = factory.smallText(localizationValue)
            sink.addInlineElement(offset, false, presentation, true)
        }

    }

}