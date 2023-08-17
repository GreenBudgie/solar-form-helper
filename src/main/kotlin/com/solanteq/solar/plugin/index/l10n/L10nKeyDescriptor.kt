package com.solanteq.solar.plugin.index.l10n

import com.intellij.util.io.KeyDescriptor
import com.solanteq.solar.plugin.l10n.L10nLocale
import org.jetbrains.kotlin.idea.core.util.readString
import org.jetbrains.kotlin.idea.core.util.writeString
import java.io.DataInput
import java.io.DataOutput

object L10nKeyDescriptor : KeyDescriptor<L10nIndexKey> {

    override fun getHashCode(value: L10nIndexKey) = value.hashCode()

    override fun isEqual(val1: L10nIndexKey, val2: L10nIndexKey) = val1 == val2

    override fun save(out: DataOutput, value: L10nIndexKey) {
        val rawString = "${value.locale.directoryName}:${value.key}"
        out.writeString(rawString)
    }

    override fun read(`in`: DataInput): L10nIndexKey {
        val rawKey = `in`.readString()
        val split = rawKey.split(':')
        if (split.size != 2) {
            throw IllegalArgumentException("Invalid index key is provided for conversion: $rawKey")
        }
        val locale = L10nLocale.getByDirectoryName(split[0])
            ?: throw IllegalArgumentException("Cannot extract locale from provided index key: $rawKey")
        val l10nKey = split[1]
        return L10nIndexKey(l10nKey, locale)
    }

}