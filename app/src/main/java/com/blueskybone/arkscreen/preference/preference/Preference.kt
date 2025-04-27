package com.blueskybone.arkscreen.preference.preference

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
interface Preference<T> {
    fun key(): String
    fun get(): T
    fun set(value: T)

    fun defaultValue(): T
    fun isSet(): Boolean

    fun delete()
}