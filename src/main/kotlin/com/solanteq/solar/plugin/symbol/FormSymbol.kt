package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.PsiUsage
import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.Pointer
import com.intellij.navigation.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
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
open class FormSymbol(
    val element: JsonStringLiteral
) : RenameTarget, SearchTarget, NavigatableSymbol {

    val project = element.project

    val file = element.containingFile as? JsonFile ?: throw IllegalArgumentException(
            "Cannot find file of a symbol underlying element. Is it a valid physical element?"
        )

    val virtualFile = file.virtualFile ?: throw IllegalArgumentException(
        "Cannot find virtual file of a symbol underlying element. Is it a valid physical element?"
    )

    val elementTextRange = element.textRangeWithoutQuotes

    val fileTextRange by lazy {
        elementTextRange.shiftRight(
            element.textRange.startOffset
        )
    }

    override fun createPointer(): Pointer<FormSymbol> {
        val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(element)
        return FormSymbolPointer(pointer)
    }

    override val targetName = element.value

    override val usageHandler = UsageHandler.createEmptyUsageHandler(element.value)

    override fun getNavigationTargets(project: Project): List<NavigationTarget> {
        return FormSymbolNavigationTarget(element).asList()
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

    class FormSymbolPointer(
        private val basePointer: SmartPsiElementPointer<JsonStringLiteral>
    ) : Pointer<FormSymbol> {

        override fun dereference() = basePointer.element?.let {
            FormSymbol(it)
        }

    }

    protected inner class FormSymbolNavigationTarget(
        val element: JsonStringLiteral
    ) : NavigationTarget {

        private val offset by lazy {
            element.textRange.startOffset + 1
        }

        override fun createPointer() = Pointer.hardPointer(this)

        override fun presentation(): TargetPresentation {
            return this@FormSymbol.presentation()
        }

        override fun navigationRequest(): NavigationRequest? {
            return NavigationService.instance().sourceNavigationRequest(
                virtualFile,
                offset
            )
        }

    }

    abstract inner class AbstractFormSymbolDeclarationPsiUsage : PsiUsage {

        override val declaration = true

        override val file: PsiFile = this@FormSymbol.file

        override val range = this@FormSymbol.fileTextRange

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

}