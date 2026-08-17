package com.blueskybone.arkscreen.data.local.pref.preference

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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

    fun flow(): Flow<T>

    fun stateIn(scope: CoroutineScope): StateFlow<T>
}