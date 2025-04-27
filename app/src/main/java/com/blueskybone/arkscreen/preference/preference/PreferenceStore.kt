package com.blueskybone.arkscreen.preference.preference

import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
interface PreferenceStore {
    fun getString(key: String, value: String): Preference<String>

    fun getInt(key: String, value: Int): Preference<Int>

    fun getLong(key: String, value: Long): Preference<Long>

    fun getFloat(key: String, value: Float): Preference<Float>

    fun getBoolean(key: String, value: Boolean): Preference<Boolean>

    fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: Function<T, String>,
        deserializer: Function<String, T>
    ): Preference<T>
}