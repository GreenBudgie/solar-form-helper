package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Pointer
import com.intellij.navigation.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.SmartPsiFileRange
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage
import com.intellij.refactoring.rename.api.RenameTarget
import com.solanteq.solar.plugin.util.asList
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes
import org.jetbrains.kotlin.idea.base.util.allScope

/**
 * Represents a symbol in a form.
 *
 * Symbols in forms can only be [JsonStringLiteral] values of properties.
 */
class FormSymbol private constructor(
    val element: JsonStringLiteral,
    val fileTextRange: TextRange,
    val type: FormSymbolType
) : RenameTarget, SearchTarget, NavigatableSymbol {

    val project = element.project

    val file = element.containingFile as? JsonFile ?: throw IllegalArgumentException(
            "Cannot find file of a symbol underlying element. Is it a valid physical element?"
        )

    val virtualFile: VirtualFile? = file.virtualFile

    val elementTextRange = fileTextRange.shiftLeft(element.textRange.startOffset)

    override fun createPointer(): Pointer<FormSymbol> {
        val manager = SmartPointerManager.getInstance(project)
        val elementPointer = manager.createSmartPsiElementPointer(element, file)
        val textRangePointer = manager.createSmartPsiFileRangePointer(file, fileTextRange)
        return FormSymbolPointer(elementPointer, textRangePointer)
    }

    override val targetName = elementTextRange.substring(element.text)

    override val usageHandler = UsageHandler.createEmptyUsageHandler(targetName)

    override fun getNavigationTargets(project: Project): List<NavigationTarget> {
        return FormSymbolNavigationTarget().asList()
    }

    override fun presentation(): TargetPresentation {
        return TargetPresentation.builder(targetName).presentation()
    }

    override val maximalSearchScope = project.allScope()

    override fun equals(other: Any?): Boolean {
        if(other !is FormSymbol) return false
        return other.element == element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    fun toDeclarationUsage() = FormSymbolDeclarationUsage()

    fun toDeclarationRenameUsage() = FormSymbolDeclarationRenameUsage()

    inner class FormSymbolPointer(
        private val baseElementPointer: SmartPsiElementPointer<JsonStringLiteral>,
        private val baseRangePointer: SmartPsiFileRange,
    ) : Pointer<FormSymbol> {

        override fun dereference(): FormSymbol? {
            val element = baseElementPointer.element ?: return null
            val fileTextRangeSegment = baseRangePointer.range ?: return null
            val fileTextRange = TextRange.create(fileTextRangeSegment)
            return FormSymbol(element, fileTextRange, type)
        }

    }

    inner class FormSymbolNavigationTarget : NavigationTarget {

        private val offset by lazy {
            fileTextRange.startOffset
        }

        override fun createPointer() = Pointer.hardPointer(this)

        override fun presentation(): TargetPresentation {
            return this@FormSymbol.presentation()
        }

        override fun navigationRequest(): NavigationRequest? {
            virtualFile ?: return null
            return NavigationService.instance().sourceNavigationRequest(
                virtualFile,
                offset
            )
        }

    }

    abstract inner class AbstractFormSymbolDeclarationPsiUsage : PsiUsage {

        override val declaration = true

        override val file: PsiFile = this@FormSymbol.file

        override val range = fileTextRange

    }

    inner class FormSymbolDeclarationUsage :
        AbstractFormSymbolDeclarationPsiUsage() {

        override fun createPointer() = Pointer.hardPointer(this)

    }

    inner class FormSymbolDeclarationRenameUsage :
        AbstractFormSymbolDeclarationPsiUsage(),
        PsiModifiableRenameUsage {

        override fun createPointer() = Pointer.hardPointer(this)

    }

    companion object {

        /**
         * Creates a form symbol using full text range of its value (does not include quotes)
         */
        fun withFullTextRange(element: JsonStringLiteral, type: FormSymbolType): FormSymbol {
            return withElementTextRange(element, element.textRangeWithoutQuotes, type)
        }

        /**
         * Creates a form symbol using the specified text range in element.
         * Note that this range includes quotes.
         */
        fun withElementTextRange(element: JsonStringLiteral,
                                 elementTextRange: TextRange,
                                 type: FormSymbolType): FormSymbol {
            return withFileTextRange(
                element,
                elementTextRange.shiftRight(element.textRange.startOffset),
                type
            )
        }

        /**
         * Creates a form symbol using the specified text range in the file
         */
        fun withFileTextRange(element: JsonStringLiteral,
                              fileTextRange: TextRange,
                              type: FormSymbolType): FormSymbol {
            return FormSymbol(element, fileTextRange, type)
        }

    }

}