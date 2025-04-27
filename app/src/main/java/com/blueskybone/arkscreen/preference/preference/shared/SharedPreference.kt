package com.blueskybone.arkscreen.preference.preference.shared

import android.content.SharedPreferences
import com.blueskybone.arkscreen.preference.preference.Preference
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
abstract class SharedPreference<T>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: T
) : Preference<T> {

    abstract fun read(preferences: SharedPreferences, key: String, defaultValue: T): T
    abstract fun write(preferences: SharedPreferences, key: String, value: T?)

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return read(preferences, key, defaultValue)
    }

    override fun set(value: T) {
        write(preferences, key, value)
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun isSet(): Boolean {
        return preferences.contains(key)
    }

    override fun delete() {
        preferences.edit().remove(key).apply()
    }

    internal class StringPrimitive(
        preferences: SharedPreferences,
        key: String,
        defaultValue: String
    ) : SharedPreference<String>(preferences, key, defaultValue) {

        override fun read(
            preferences: SharedPreferences,
            key: String,
            defaultValue: String
        ): String {
            return preferences.getString(key, defaultValue) ?: defaultValue
        }
        override fun write(preferences: SharedPreferences, key: String, value: String?) {
            preferences.edit()?.putString(key, value)?.apply()
        }
    }


    internal class LongPrimitive(
        preferences: SharedPreferences,
        key: String,
        defaultValue: Long
    ) : SharedPreference<Long>(preferences, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Long): Long {
            return preferences.getLong(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Long?) {
            preferences.edit().putLong(key, value!!).apply()
        }
    }

    internal class IntegerPrimitive(
        preferences: SharedPreferences,
        key: String,
        defaultValue: Int
    ) :
        SharedPreference<Int>(preferences, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Int): Int {
            return preferences.getInt(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Int?) {
            preferences.edit().putInt(key, value!!).apply()
        }
    }

    internal class FloatPrimitive(
        preferences: SharedPreferences,
        key: String,
        defaultValue: Float
    ) : SharedPreference<Float>(preferences, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Float): Float {
            return preferences.getFloat(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Float?) {
            preferences.edit().putFloat(key, value!!)?.apply()
        }
    }

    internal class BooleanPrimitive(
        preferences: SharedPreferences,
        key: String,
        defaultValue: Boolean
    ) : SharedPreference<Boolean>(preferences, key, defaultValue) {
        override fun read(
            preferences: SharedPreferences,
            key: String,
            defaultValue: Boolean
        ): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Boolean?) {
            preferences.edit().putBoolean(key, value!!).apply()
        }

    }

    internal class Object<T>(
        preferences: SharedPreferences,
        key: String,
        defaultValue: T,
        private val serializer: Function<T, String>,
        private val deserializer: Function<String, T>
    ) : SharedPreference<T>(preferences, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: T): T {
            val str = preferences.getString(key, null)
            return try {
                deserializer.apply(str!!)
            } catch (e: Exception) {
                defaultValue!!
            }
        }
        override fun write(preferences: SharedPreferences, key: String, value: T?) {
            preferences.edit().putString(key, serializer.apply(value!!)).apply()
        }
    }
}

