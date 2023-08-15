package com.solanteq.solar.plugin.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

/**
 * An index that stores java and kotlin source files that can be probably considered implementations of
 * callable services. This index does not guarantee that these classes really implement `@CallableService` interface
 * because we cannot look into other files when building an index. So this index stores all files that
 * contain `@Service` annotation with value. This still allows to search for the specified service
 * significantly faster.
 * - Key: name of the callable service, as specified in @Service annotation. For example: `test.testService`
 * - Value: a list of files that contain a class with @Service annotation with its
 * value set to the specified key.
 */
class CallableServiceImplIndex : ScalarIndexExtension<String>() {

    override fun getName() = NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile.toUElementOfType<UFile>() ?: return@DataIndexer emptyMap()

        file.classes
            .flatMap { it.uAnnotations }
            .filter { checkAnnotationName(it) }
            .mapNotNull {getAnnotationValue(it) }
            .associateWith { null }
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == KotlinFileType.INSTANCE || it.fileType == JavaFileType.INSTANCE
    }

    override fun dependsOnFileContent() = true

    private fun checkAnnotationName(annotation: UAnnotation): Boolean {
        val annotationFullText = annotation.sourcePsi?.text ?: return false
        if(annotationFullText.length < 9) {
            return false
        }
        val isAnnotationTextValid = annotationFullText[0] == '@' && annotationFullText[8] == '('
        if(!isAnnotationTextValid) {
            return false
        }
        val serviceSubstring = annotationFullText.substring(1, 8)
        return serviceSubstring == "Service"
    }

    private fun getAnnotationValue(annotation: UAnnotation): String? {
        val annotationFullText = annotation.sourcePsi?.text ?: return null
        val valueRegex = Regex(ANNOTATION_VALUE_REGEX)
        val matchResult = valueRegex.find(annotationFullText)
        val groupValues = matchResult?.groupValues ?: return null
        (1..3).forEach {
            if(groupValues[it].isNotBlank()) {
                return groupValues[it]
            }
        }
        return null
    }

    companion object {

        private const val ANNOTATION_VALUE_REGEX =
            "@\\w+\\((?=.*value\\s*=\\s*\"(.*?)\").*?\"(.*?)\"\\)|@\\w+\\(\"(.+?)\".*\\)"

        val NAME = ID.create<String, Void>("CallableServiceImplIndex")

        /**
         * Returns the list of [VirtualFile]s that contain a class with @Service annotation with
         * value that equals to [callableServiceName].
         *
         * Note that found files are not guaranteed to contain callable services,
         * so double-checking is required. Consider reading [CallableServiceImplIndex] documentation
         * for more details.
         */
        fun getFilesContainingCallableServiceImpl(callableServiceName: String,
                                                  scope: GlobalSearchScope): Collection<VirtualFile> =
            FileBasedIndex.getInstance().getContainingFiles(
                NAME,
                callableServiceName,
                scope
            )

        /**
         * Gets all files that can **possibly** contain a callable service.
         *
         * Consider reading [CallableServiceImplIndex] documentation for more details.
         */
        fun getAllPossibleFilesWithCallableServices(project: Project): Set<VirtualFile> {
            val fileBasedIndex = FileBasedIndex.getInstance()
            val allServices = mutableSetOf<VirtualFile>()
            val allScope = project.allScope()
            fileBasedIndex.processAllKeys(NAME, { serviceName ->
                val files = fileBasedIndex.getContainingFiles(
                    NAME,
                    serviceName,
                    allScope
                )
                allServices.addAll(files)
                true
            }, project)
            return allServices
        }

    }

}