package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.json.psi.JsonFile
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiFileRange
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage

/**
 * An implementation of both [PsiUsage] and [PsiModifiableRenameUsage] for [FormSymbol] usages/declarations
 */
class FormSymbolUsage(
    override val file: JsonFile,
    override val range: TextRange,
    override val declaration: Boolean
) : PsiUsage, PsiModifiableRenameUsage {

    constructor(
        symbol: FormSymbol,
        declaration: Boolean
    ) : this(
        symbol.file.originalFile as JsonFile,
        symbol.fileTextRange,
        declaration
    )

    constructor(
        reference: FormSymbolReference
    ) : this(
        reference.element.containingFile.originalFile as JsonFile,
        reference.absoluteRange,
        false
    )

    override fun createPointer(): Pointer<FormSymbolUsage> {
        val pointer = SmartPointerManager
            .getInstance(file.project)
            .createSmartPsiFileRangePointer(file, range)
        return FormSymbolUsagePointer(pointer)
    }

    inner class FormSymbolUsagePointer(
        private val basePointer: SmartPsiFileRange
    ) : Pointer<FormSymbolUsage> {

        override fun dereference(): FormSymbolUsage? {
            val file = basePointer.element as? JsonFile ?: return null
            val rangeSegment = basePointer.range ?: return null
            val textRange = TextRange.create(rangeSegment)
            return FormSymbolUsage(file, textRange, declaration)
        }

    }

}