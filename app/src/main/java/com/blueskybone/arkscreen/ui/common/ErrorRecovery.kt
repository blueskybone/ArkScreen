package com.blueskybone.arkscreen.ui.common

enum class ErrorRecovery {
    RETRY,
    RELOGIN,
    VIEW_LOGS,
}

fun recoveryFor(message: String): ErrorRecovery {
    val normalized = message.lowercase()
    return when {
        looksLikeNetworkFailure(normalized) || looksLikeTimeout(normalized) ->
            ErrorRecovery.RETRY

        listOf(
            "token", "grant", "cred", "401", "403", "凭证",
            "登录过期", "登录状态已过期", "授权失败",
        ).any(normalized::contains) -> ErrorRecovery.RELOGIN

        listOf("json", "解析", "数据库", "database", "格式错误")
            .any(normalized::contains) -> ErrorRecovery.VIEW_LOGS

        else -> ErrorRecovery.RETRY
    }
}

fun userFacingError(message: String): String {
    val normalized = message.lowercase()
    return when {
        looksLikeNetworkFailure(normalized) -> "网络不可用"
        looksLikeTimeout(normalized) -> "请求超时"
        recoveryFor(message) == ErrorRecovery.RELOGIN -> "登录状态已过期"
        else -> message
    }
}

private fun looksLikeNetworkFailure(message: String): Boolean =
    listOf(
        "unable to resolve host",
        "unknownhost",
        "failed to connect",
        "network is unreachable",
        "network unavailable",
        "网络不可用",
        "网络连接失败",
    ).any(message::contains)

private fun looksLikeTimeout(message: String): Boolean =
    listOf("timeout", "timed out", "请求超时").any(message::contains)
