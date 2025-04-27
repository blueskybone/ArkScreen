package com.blueskybone.arkscreen.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */

@Entity
data class Link(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var title: String,
    var url: String
)