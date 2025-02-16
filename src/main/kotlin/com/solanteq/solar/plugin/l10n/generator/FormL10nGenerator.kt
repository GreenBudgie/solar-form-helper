package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.l10n.L10nKey
import com.solanteq.solar.plugin.search.FormGraphSearch

object FormL10nGenerator {

    fun getBestPlacementFor(element: FormLocalizableElement<*>, key: L10nKey): L10nGenerator.Placement? {


        TODO()
    }

    fun getBestPlacementInL10nFile(
        element: FormLocalizableElement<*>,
        key: L10nKey,
        l10nFile: JsonFile,
    ): L10nGenerator.Placement? {
        val visitor = LocalizableElementVisitor(element)
        l10nFile.accept(visitor)



        TODO()
    }

    /**
     * Searches for all localizable elements
     */
    private class LocalizableElementVisitor(
        private val element: FormLocalizableElement<*>,
    ) : JsonRecursiveElementVisitor() {

        var prevLocalizableElement: FormLocalizableElement<*>? = null
            private set
        var nextLocalizableElement: FormLocalizableElement<*>? = null
            private set

        private var processing = true

        override fun visitElement(o: JsonElement) {
            if (!processing) {
                return
            }

            super.visitElement(o)

            val localizableElement = FormElementFactory.createLocalizableElement(o)
            if (localizableElement == null) {
                return
            }

            if (element != localizableElement) {
                prevLocalizableElement = localizableElement
                return
            }


            processing = false
        }

    }

}