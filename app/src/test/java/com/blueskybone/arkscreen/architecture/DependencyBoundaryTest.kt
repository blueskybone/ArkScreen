package com.blueskybone.arkscreen.architecture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs with the normal unit-test task; covers every handwritten source set, not generated code. */
class DependencyBoundaryTest {
    private val sourceSets = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
        .flatMap { sequenceOf(File(it, "src"), File(it, "app/src")) }
        .first { File(it, "main/java/com/blueskybone/arkscreen").isDirectory }

    private fun sources(): List<Pair<File, File>> = sourceSets.listFiles().orEmpty().flatMap { sourceSet ->
        listOf("java", "kotlin").flatMap { directory ->
            val root = File(sourceSet, directory)
            root.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "java") }
                .map { root to it }.toList()
        }
    }

    @Test fun sourcesExist() {
        assertTrue("Architecture checks must not silently skip missing source roots", sources().isNotEmpty())
    }

    @Test fun dependencyBoundariesPointInward() {
        check { path, source -> ArchitectureRules.forbiddenDependencies(path, source) }
    }

    @Test fun packageDeclarationsMatchDirectories() {
        check { path, source -> listOfNotNull(ArchitectureRules.packageMismatch(path, source)) }
    }

    @Test fun viewModelsUseExplicitRoleNames() {
        check { path, source -> ArchitectureRules.viewModelNamingViolations(path, source) }
    }

    private fun check(rule: (String, String) -> List<String>) {
        val violations = sources().flatMap { (root, file) ->
            rule(file.relativeTo(root).invariantSeparatorsPath, file.readText())
                .map { "${root.relativeTo(sourceSets).invariantSeparatorsPath}/$it" }
        }
        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }
}
