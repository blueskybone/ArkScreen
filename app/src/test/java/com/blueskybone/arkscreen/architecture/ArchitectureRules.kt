package com.blueskybone.arkscreen.architecture

/** Source-level guardrails for the single-module project, not a replacement for compiler checks. */
internal object ArchitectureRules {
    private const val root = "com.blueskybone.arkscreen"
    private val packageDeclaration = Regex("(?m)^\\s*package\\s+([\\w.]+)")
    // Keep newlines for useful line numbers; comments and string contents are not dependencies.
    private val nonCode = Regex("\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\n]*|/\\*[\\s\\S]*?\\*/")

    fun code(source: String): String = nonCode.replace(source) { match ->
        match.value.map { if (it == '\n') '\n' else ' ' }.joinToString("")
    }

    fun packageMismatch(relativePath: String, source: String): String? {
        val expected = relativePath.substringBeforeLast('/').replace('/', '.')
        val actual = packageDeclaration.find(code(source))?.groupValues?.get(1)
        return if (actual == expected) null else "$relativePath: expected package $expected, found ${actual ?: "<missing>"}"
    }

    fun forbiddenDependencies(relativePath: String, source: String): List<String> {
        val layer = relativePath.removePrefix(root.replace('.', '/') + "/")
        val forbidden = when {
            layer.startsWith("domain/") -> listOf("$root.data", "$root.ui", "$root.platform", "$root.di", "$root.presentation", "android", "androidx", "com.fasterxml")
            layer.startsWith("data/") -> listOf("$root.ui", "$root.presentation")
            layer.startsWith("platform/recruit/") -> listOf("$root.ui", "$root.presentation")
            layer.startsWith("ui/widget/") -> listOf("$root.data.local.room")
            else -> emptyList()
        }
        return code(source).lines().flatMapIndexed { index, line ->
            forbidden.filter { prefix -> Regex("\\b${Regex.escape(prefix)}(?:\\.|\\b)").containsMatchIn(line) }
                .map { "$relativePath:${index + 1}: forbidden dependency $it" }
        }
    }

    fun viewModelNamingViolations(relativePath: String, source: String): List<String> {
        val declaration = Regex("\\bclass\\s+(\\w+)(?:(?!\\bclass\\b)[^{}])*?:\\s*(?:androidx\\.lifecycle\\.)?ViewModel\\s*\\(")
        return declaration.findAll(code(source)).mapNotNull { match ->
            val name = match.groupValues[1]
            if (name.endsWith("ViewModel") && relativePath.substringAfterLast('/') == "$name.kt") null
            else "$relativePath: ViewModel class $name must use a *ViewModel name and matching filename"
        }.toList()
    }
}
