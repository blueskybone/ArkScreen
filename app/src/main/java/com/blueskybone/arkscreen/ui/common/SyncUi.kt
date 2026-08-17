package com.blueskybone.arkscreen.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatSyncTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
