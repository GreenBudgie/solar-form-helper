package com.solanteq.solar.plugin.bundle

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.SolarBundle"

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
internal object SolarBundle {

    private val instance = DynamicBundle(SolarBundle::class.java, BUNDLE)

    fun message(
        key: @PropertyKey(resourceBundle = BUNDLE) String,
        vararg params: Any,
    ): String {
        return instance.getMessage(key, *params)
    }

    fun lazyMessage(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): Supplier<String> {
        return instance.getLazyMessage(key, *params)
    }

}