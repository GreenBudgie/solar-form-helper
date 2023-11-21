package com.solanteq.solar.plugin.ui.editor

import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel

class FormEditorState : FileEditorState {

    private val falseExpressions = mutableSetOf<String>()

    fun setExpressionValue(expressionName: String, value: Boolean) {
        if (value) {
            falseExpressions += expressionName
        } else {
            falseExpressions.remove(expressionName)
        }
    }

    fun isExpressionTrue(expressionName: String): Boolean {
        return expressionName !in falseExpressions
    }

    override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean {
        return true
    }

}