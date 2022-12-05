package com.solanteq.solar.plugin.util

import com.intellij.json.psi.*
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.findParentOfType
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType
import org.jetbrains.kotlin.idea.base.util.allScope

/**
 * Constructs a pattern to check if the specified json element is inside the form file
 */
inline fun <reified T : JsonElement> inForm(): PsiElementPattern.Capture<out T> {
    val basePattern = PlatformPatterns.psiElement(T::class.java)
    return basePattern.andOr(
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(TopLevelFormFileType))),
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(IncludedFormFileType)))
    )
}

/**
 * Extends the pattern to check whether the element is a json value with one of the specified keys.
 *
 * Example:
 *
 * Pattern `isValueWithKey("request")` passes for
 * `"request": "lty.service.find"`, where `"lty.service.find"` is a value
 */
fun PsiElementPattern.Capture<out JsonStringLiteral>.isValueWithKey(vararg applicableKeys: String) = with(
    object : PatternCondition<JsonStringLiteral>("isValueWithKey") {
        override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
            if(!JsonPsiUtil.isPropertyValue(element)) return false
            val parentJsonProperty = element.parent as? JsonProperty ?: return false
            return parentJsonProperty.name in applicableKeys
        }
    }
)

/**
 * Extends the pattern to check whether the element is inside a json object with one of specified keys.
 *
 * Example:
 *
 * Consider the following json structure:
 * ```
 * "request": {
 *      "name": "lty.service.findById",
 *      "group": "lty",
 *      "params": [
 *          {
 *              "name": "id",
 *              "value": "id"
 *          }
 *      ]
 * }
 * ```
 * Pattern `isElementInsideObject("request")` will pass for every json element inside `"request"` object,
 * excluding `"name": "id"` and `"value": "id"` in params because they are inside their own unnamed object.
 */
fun PsiElementPattern.Capture<out JsonStringLiteral>.isInsideObjectWithKey(vararg applicableKeys: String) = with(
    object : PatternCondition<JsonStringLiteral>("isInsideObjectWithKey") {
        override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
            val firstJsonObjectParent = element.findParentOfType<JsonObject>() ?: return false
            val jsonObjectProperty = firstJsonObjectParent.parent as? JsonProperty ?: return false
            return jsonObjectProperty.name in applicableKeys
        }
    }
)

fun PsiElementPattern.Capture<out JsonStringLiteral>.isObjectInArrayWithKey(vararg applicableKeys: String) = with(
    object : PatternCondition<JsonStringLiteral>("isObjectInArrayWithKey") {
        override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
            val firstJsonArrayParent = element.findParentOfType<JsonArray>() ?: return false
            val jsonArrayProperty = firstJsonArrayParent.parent as? JsonProperty ?: return false
            return jsonArrayProperty.name in applicableKeys
        }
    }
)

private val TOP_LEVEL_FORMS_KEY = Key<CachedValue<List<VirtualFile>>>("solar.topLevelForms")
private val INCLUDED_FORMS_KEY = Key<CachedValue<List<VirtualFile>>>("solar.includedForms")

/**
 * Finds top level forms in all scope with caching
 */
fun findTopLevelForms(project: Project) =
    findForms(project, TOP_LEVEL_FORMS_KEY, TopLevelFormFileType)

/**
 * Finds included forms in all scope with caching
 */
fun findIncludedForms(project: Project) =
    findForms(project, INCLUDED_FORMS_KEY, IncludedFormFileType)

/**
 * Finds included and not included forms in all scope with caching
 */
fun findAllForms(project: Project) =
    findTopLevelForms(project) + findIncludedForms(project)

/**
 * Checks whether this virtual file is form or included form by checking its file type
 */
fun VirtualFile.isForm() = fileType == TopLevelFormFileType || fileType == IncludedFormFileType

/**
 * Checks whether this psi file is form or included form by checking its file type
 */
fun PsiFile.isForm() = fileType == TopLevelFormFileType || fileType == IncludedFormFileType

/**
 * Gets the parent directory name of this form file, or null if this file can't be treated as form.
 * This directory may correspond to a form module, but that might not be true for included forms.
 */
fun VirtualFile.getFormModule(): String? {
    if(!isForm()) {
        return null
    }

    val parentDirectory = parent
    if(!parentDirectory.isDirectory) {
        return null
    }

    val formsDirectory = parentDirectory.parent
    if(!formsDirectory.isDirectory || formsDirectory.name != "forms") {
        return null
    }

    return parentDirectory.name
}

/**
 * @see getFormModule
 */
fun PsiFile.getFormModule() = virtualFile?.getFormModule()

/**
 * Gets the form name of this virtual file, or null if this file can't be treated as form
 * @see isForm
 */
fun VirtualFile.getFormSolarName(): String {
    val formModule = getFormModule() ?: return nameWithoutExtension

    return "$formModule.${nameWithoutExtension}"
}

/**
 * @see getFormSolarName
 */
fun PsiFile.getFormSolarName() = virtualFile?.getFormSolarName()

/**
 * Finds a form by its full name in all scope, or null if not found
 */
fun findFormByFullName(
    project: Project,
    fullName: String,
    scope: GlobalSearchScope = project.allScope()
): VirtualFile? {
    val (module, name) = getModuleAndNameByFormName(fullName) ?: return null
    return findFormByModuleAndName(project, module, name, scope)
}

/**
 * Finds a form by its module and name in all scope, or null if not found
 */
fun findFormByModuleAndName(
    project: Project,
    module: String,
    name: String,
    scope: GlobalSearchScope = project.allScope()
): VirtualFile? {
    val applicableFilesByName = FilenameIndex.getVirtualFilesByName(
        "$name.json",
        scope
    )
    return applicableFilesByName.firstOrNull {
        it.getFormSolarName() == "$module.$name"
    }
}

/**
 * Gets module and name by form full name, or null if the specified name has invalid format.
 *
 * Example:
 * ```
 * val (module, name) = getModuleAndNameByFormName("test.form") ?: return null
 * -> module = test
 * -> name = form
 * ```
 */
fun getModuleAndNameByFormName(fullName: String): Pair<String, String>? {
    val splitName = fullName.split(".")
    if(splitName.size != 2) {
        return null
    }
    return splitName[0] to splitName[1]
}

private fun findForms(
    project: Project,
    key: Key<CachedValue<List<VirtualFile>>>,
    fileType: FileType
): List<VirtualFile> {
    return CachedValuesManager.getManager(project).getCachedValue(
        project,
        key,
        {
            val jsonFiles = FilenameIndex.getAllFilesByExt(project, "json")
            CachedValueProvider.Result(
                jsonFiles.filter { it.fileType == fileType }.toList(),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
            )
        },
        false)
}









