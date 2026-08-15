package com.blueskybone.arkscreen.core.logger

object LogSanitizer {
    private val sensitiveHeader = Regex(
        "(?i)^(authorization|cookie|set-cookie|cred|token|did|x-account-token|x-role-token|x-signature|x-sign-header):\\s*.*$",
        RegexOption.MULTILINE,
    )
    private val sensitiveJson = Regex(
        "(?i)(\\\"(?:password|phone|token|cred|cookie|did|ak-user-center|xr-token)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
    )
    private val sensitiveQuery = Regex(
        "(?i)([?&](?:password|phone|token|cred|cookie|did|uid|roleid|serverid|" +
            "sign|signature|w_rid|ak-user-center|xr-token)=)[^&\\s]*",
    )

    fun sanitize(message: String): String = message
        .replace(sensitiveHeader) { match -> "${match.groupValues[1]}: ██" }
        .replace(sensitiveJson, "$1██$2")
        .replace(sensitiveQuery, "$1██")
}
