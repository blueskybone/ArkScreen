package com.blueskybone.arkscreen.architecture

import org.junit.Assert.*
import org.junit.Test

class ArchitectureRulesTest {
    private val root = "com/blueskybone/arkscreen"

    @Test fun rejectsEachForbiddenDomainLayerIncludingQualifiedReferencesAndAliases() {
        listOf("data", "ui", "platform").forEach { layer ->
            assertTrue(ArchitectureRules.forbiddenDependencies("$root/domain/Example.kt",
                "import com.blueskybone.arkscreen.$layer.Example as Alias").isNotEmpty())
            assertTrue(ArchitectureRules.forbiddenDependencies("$root/domain/Example.java",
                "class Example { com.blueskybone.arkscreen.$layer.Example field; }").isNotEmpty())
        }
    }

    @Test fun rejectsDataToUiButAllowsInwardImports() {
        assertTrue(ArchitectureRules.forbiddenDependencies("$root/data/Example.kt",
            "import com.blueskybone.arkscreen.ui.Example").isNotEmpty())
        assertTrue(ArchitectureRules.forbiddenDependencies("$root/data/Example.kt",
            "import com.blueskybone.arkscreen.domain.model.Example").isEmpty())
    }

    @Test fun commentsAndTextAreNotDependencies() {
        val source = "// com.blueskybone.arkscreen.ui.Example\n" +
            "/* com.blueskybone.arkscreen.data.Example */\n" +
            "val text = \"com.blueskybone.arkscreen.platform.Example\""
        assertTrue(ArchitectureRules.forbiddenDependencies("$root/domain/Example.kt", source).isEmpty())
    }

    @Test fun detectsMissingOrMisplacedPackageInKotlinAndJava() {
        assertNotNull(ArchitectureRules.packageMismatch("$root/data/Example.kt", "class Example"))
        assertNotNull(ArchitectureRules.packageMismatch("$root/data/Example.kt", "package com.blueskybone.arkscreen.data.remote"))
        assertNull(ArchitectureRules.packageMismatch("$root/data/Example.java", "package com.blueskybone.arkscreen.data;"))
    }

    @Test fun adjacentDataClassIsNotMistakenForViewModel() {
        assertTrue(ArchitectureRules.viewModelNamingViolations("ui/ExampleViewModel.kt",
            "data class Data(val value: String)\nclass ExampleViewModel : ViewModel() {}").isEmpty())
    }

    @Test fun viewModelNamesAndFilenamesMustDescribeTheirRole() {
        assertTrue(ArchitectureRules.viewModelNamingViolations("ui/ExampleModel.kt",
            "class ExampleModel(private val repo: Repo) : ViewModel() {}").isNotEmpty())
        assertTrue(ArchitectureRules.viewModelNamingViolations("ui/Wrong.kt",
            "class ExampleViewModel : ViewModel() {}").isNotEmpty())
        assertTrue(ArchitectureRules.viewModelNamingViolations("ui/ExampleViewModel.kt",
            "class ExampleViewModel(private val repo: Repo) : ViewModel() {}").isEmpty())
    }
}
