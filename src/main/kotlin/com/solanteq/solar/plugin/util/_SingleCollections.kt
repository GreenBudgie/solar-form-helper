/**
 * Many operations require to convert a single element to list or array.
 * Wrapping single element with `listOf()` or `Collections.singletonList()`
 * makes the code a bit harder to read.
 * It is more convenient to have extension methods for creating single-element collections.
 */
package com.solanteq.solar.plugin.util

import java.util.*

/**
 * Wraps the given element as set or returns empty set if the element is null
 */
fun <T> T?.asSetOrEmpty(): Set<T> = this?.let { Collections.singleton(it) } ?: emptySet()

/**
 * Wraps the given element as set
 */
fun <T> T.asSet(): Set<T> = Collections.singleton(this)

/**
 * Wraps the given element as list or returns empty list if the element is null
 */
fun <T> T?.asListOrEmpty(): List<T> = this?.let { Collections.singletonList(it) } ?: emptyList()

/**
 * Wraps the given element as list
 */
fun <T> T.asList(): List<T> = Collections.singletonList(this)

/**
 * Wraps the given element as array or returns empty array if the element is null
 */
inline fun <reified T> T?.asArrayOrEmpty(): Array<T> = this?.let { arrayOf(it) } ?: emptyArray()

/**
 * Wraps the given element as array
 */
inline fun <reified T> T.asArray(): Array<T> = arrayOf(this)

