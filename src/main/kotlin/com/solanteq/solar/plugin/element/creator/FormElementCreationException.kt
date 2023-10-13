package com.solanteq.solar.plugin.element.creator

import com.intellij.json.psi.JsonElement
import kotlin.reflect.KClass

class FormElementCreationException(
    formElementClass: KClass<*>,
    providedSourceElement: JsonElement?
) : IllegalArgumentException(
    "${formElementClass.simpleName} cannot be created from ${providedSourceElement?.name ?: providedSourceElement.toString()}"
)