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
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.findParentOfType
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType
import org.jetbrains.kotlin.idea.search.allScope

/**
 * Constructs a pattern to check if the specified json element is inside the form file
 */
fun <T : JsonElement> inForm(psiElementClass: Class<out T>): PsiElementPattern.Capture<out T> {
    val basePattern = PlatformPatterns.psiElement(psiElementClass)
    return basePattern.andOr(
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(FormFileType))),
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

private val NOT_INCLUDED_FORMS_KEY = Key<CachedValue<List<VirtualFile>>>("solar.notIncludedForms")
private val INCLUDED_FORMS_KEY = Key<CachedValue<List<VirtualFile>>>("solar.includedForms")

/**
 * Finds not included forms in all scope with caching
 */
fun findNotIncludedForms(project: Project) =
    findForms(project, NOT_INCLUDED_FORMS_KEY, FormFileType)

/**
 * Finds included forms in all scope with caching
 */
fun findIncludedForms(project: Project) =
    findForms(project, INCLUDED_FORMS_KEY, IncludedFormFileType)

/**
 * Finds included and not included forms in all scope with caching
 */
fun findAllForms(project: Project) =
    findNotIncludedForms(project) + findIncludedForms(project)

/**
 * Checks whether this virtual file is form or included form by checking its file type
 */
fun VirtualFile.isForm() = fileType == FormFileType || fileType == IncludedFormFileType

/**
 * Checks whether this psi file is form or included form by checking its file type
 */
fun PsiFile.isForm() = fileType == FormFileType || fileType == IncludedFormFileType

/**
 * Gets the group of this form, or null if this file can't be treated as form
 */
fun VirtualFile.getFormGroup(): String? {
    if(!isForm()) {
        return null
    }

    val parentDirectory = parent
    if(!parentDirectory.isDirectory) {
        return null
    }

    val parentDirectoryName = parentDirectory.name
    if(parentDirectoryName == "forms") {
        return null
    }

    return parentDirectoryName
}

/**
 * @see getFormGroup
 */
fun PsiFile.getFormGroup() = virtualFile?.getFormGroup()

/**
 * Gets the form name of this virtual file, or null if this file can't be treated as form
 * @see isForm
 */
fun VirtualFile.getFormSolarName(): String? {
    val formGroup = getFormGroup() ?: return null

    return "$formGroup.${nameWithoutExtension}"
}

/**
 * @see getFormSolarName
 */
fun PsiFile.getFormSolarName() = virtualFile?.getFormSolarName()

/**
 * Finds a form by its full name in all scope, or null if not found
 */
fun findFormByFullName(project: Project, fullName: String): VirtualFile? {
    val (group, name) = getGroupAndNameByFormName(fullName) ?: return null
    return findFormByGroupAndName(project, group, name)
}

/**
 * Finds a form by its group and name in all scope, or null if not found
 */
fun findFormByGroupAndName(project: Project, group: String, name: String): VirtualFile? {
    val applicableFilesByName = FilenameIndex.getVirtualFilesByName("$name.json", project.allScope())
    return applicableFilesByName.firstOrNull {
        it.getFormSolarName() == "$group.$name"
    }
}

/**
 * Gets group and name by form full name, or null if the specified name has invalid format.
 *
 * Example:
 * ```
 * val (group, name) = getGroupAndNameByFormName("test.form") ?: return null
 * -> group = test
 * -> name = form
 * ```
 */
fun getGroupAndNameByFormName(fullName: String): Pair<String, String>? {
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









