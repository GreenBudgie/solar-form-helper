package com.solanteq.solar.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

inline fun <T> cacheByKey(
    project: Project,
    key: Key<CachedValue<T>>,
    modificationTracker: ModificationTracker,
    crossinline computation: () -> T
) : T {
    return CachedValuesManager.getManager(project).getCachedValue(
        project,
        key,
        {
            CachedValueProvider.Result(
                computation(),
                modificationTracker
            )
        },
        false
    )
}