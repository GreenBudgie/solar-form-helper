package com.solanteq.solar.plugin.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

/**
 * An index that stores java and kotlin dropdown enum classes
 * - Key: dropdown name with module, for example: "crm.visaTaxIdType"
 * - Value: a list files containing `@Dropdown` annotated enum class with the given name (key)
 */
class DropdownIndex : ScalarIndexExtension<String>() {

    override fun getName() = NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile.toUElementOfType<UFile>() ?: return@DataIndexer emptyMap()
        file.classes.forEach {
            val dropdownModule = getDropdownAnnotationModule(it) ?: return@forEach
            val className = it.namedUnwrappedElement?.name ?: return@forEach
            val modifiedDropdownClassName = className.replaceFirstChar { char -> char.lowercaseChar() }
            val dropdownSolarName = "$dropdownModule.$modifiedDropdownClassName"
            return@DataIndexer mapOf(dropdownSolarName to null)
        }
        return@DataIndexer emptyMap()
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == KotlinFileType.INSTANCE || it.fileType == JavaFileType.INSTANCE
    }

    override fun dependsOnFileContent() = true

    private fun getDropdownAnnotationModule(uClass: UClass): String? {
        uClass.uAnnotations.forEach {
            val text = it.sourcePsi?.text ?: return@forEach
            if(!isDropdownAnnotation(text)) {
                return@forEach
            }
            return getModuleFromAnnotation(text)
        }
        return null
    }

    private fun isDropdownAnnotation(annotationText: String): Boolean {
        if(annotationText.length < 10) {
            return false
        }
        val isAnnotationTextValid = annotationText[0] == '@' && annotationText[9] == '('
        if(!isAnnotationTextValid) {
            return false
        }
        val serviceSubstring = annotationText.substring(1, 9)
        return serviceSubstring == "Dropdown"
    }

    private fun getModuleFromAnnotation(annotationText: String): String? {
        val moduleRegex = Regex(ANNOTATION_MODULE_REGEX)
        val matchResult = moduleRegex.find(annotationText)
        return matchResult?.groupValues?.getOrNull(1)
    }

    companion object {

        private const val ANNOTATION_MODULE_REGEX = "@Dropdown\\(module\\s*=\\s*\"([^\"]+)\""

        val NAME = ID.create<String, Void>("DropdownIndex")

        /**
         * Returns the list of [VirtualFile]s that contain an enum class with @Dropdown annotation and
         * the dropdown name is equal to [dropdownFullName].
         *
         * Note that found files are not guaranteed to contain dropdowns, so double-checking is required.
         */
        fun getFilesContainingDropdown(dropdownFullName: String,
                                       scope: GlobalSearchScope): Collection<VirtualFile> =
            FileBasedIndex.getInstance().getContainingFiles(
                NAME,
                dropdownFullName,
                scope
            )

        /**
         * Gets all files that can **possibly** contain dropdowns
         */
        fun getAllPossibleFilesWithDropdowns(project: Project): Set<VirtualFile> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            val allDropdowns = mutableSetOf<VirtualFile>()
            val allScope = project.allScope()
            fileBasedIndex.processAllKeys(NAME, { dropdownName ->
                val files = fileBasedIndex.getContainingFiles(
                    NAME,
                    dropdownName,
                    allScope
                )
                allDropdowns.addAll(files)
                true
            }, project)
            return allDropdowns
        }

    }

}