package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.json.psi.JsonFile
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
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
        symbol.file,
        symbol.fileTextRange,
        declaration
    )

    constructor(
        reference: FormSymbolReference<*>
    ) : this(
        reference.element.containingFile as JsonFile,
        reference.absoluteRange,
        false
    )

    override fun createPointer() = Pointer.hardPointer(this)

}