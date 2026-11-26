package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import com.blueskybone.arkscreen.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class WidgetEditorViewModelTest {
    @Test fun loadingOlderWidgetCannotOverwriteNewSelection() = runBlocking {
        fixture { model, repository ->
            model.load(1)
            drain()
            model.load(2)
            drain()
            repository.loads.getValue(2).complete(WidgetConfig(2, theme = "dark"))
            drain()
            repository.loads.getValue(1).complete(WidgetConfig(1))
            drain()
            assertEquals(2, model.state.value.draft?.appWidgetId)
            assertEquals("dark", model.state.value.draft?.theme)
            assertFalse(model.state.value.loading)
        }
    }

    @Test fun restoredDraftRemainsUnsavedAndSurvivesSameWidgetLoad() = runBlocking {
        fixture { model, repository ->
            model.load(1) { it.copy(theme = "mist", transparency = 40) }
            drain()
            repository.loads.getValue(1).complete(WidgetConfig(1, theme = "dark"))
            drain()
            assertEquals("dark", model.state.value.original?.theme)
            assertEquals("mist", model.state.value.draft?.theme)
            assertTrue(model.state.value.dirty)
            model.load(1)
            drain()
            assertEquals(40, model.state.value.draft?.transparency)
            assertEquals(1, repository.loadCount)
        }
    }

    @Test fun saveCommitsSnapshotWithoutDiscardingNewerEdits() = runBlocking {
        fixture { model, repository ->
            model.load(1)
            drain()
            repository.loads.getValue(1).complete(WidgetConfig(1))
            drain()
            model.updateDraft(model.state.value.draft!!.copy(theme = "dark"))
            model.save()
            model.save()
            drain()
            assertEquals(1, repository.saved.size)
            model.updateDraft(model.state.value.draft!!.copy(theme = "mist"))
            repository.saveResult.complete(Result.success(Unit))
            drain()
            assertEquals("dark", model.state.value.original?.theme)
            assertEquals("mist", model.state.value.draft?.theme)
            assertTrue(model.state.value.dirty)
            assertFalse(model.state.value.saving)
            assertEquals(WidgetEditorEvent.Saved(1), model.event.first())
        }
    }

    @Test fun failedSaveRetainsDirtyDraft() = runBlocking {
        fixture { model, repository ->
            model.load(1)
            drain()
            repository.loads.getValue(1).complete(WidgetConfig(1))
            drain()
            model.updateDraft(model.state.value.draft!!.copy(fontScale = 1.1f))
            model.save()
            drain()
            repository.saveResult.complete(Result.failure(IllegalStateException("disk")))
            drain()
            assertTrue(model.state.value.dirty)
            assertEquals(1.1f, model.state.value.draft!!.fontScale)
            assertFalse(model.state.value.saving)
            assertTrue(model.event.first() is WidgetEditorEvent.Error)
        }
    }

    private suspend fun drain() { repeat(8) { yield() } }
    private suspend fun CoroutineScope.fixture(block: suspend (WidgetEditorViewModel, Configs) -> Unit) {
        val job = SupervisorJob(coroutineContext[Job])
        val repository = Configs()
        val accounts = stub<AccountRepository> { name ->
            if (name == "observeSkAcc") flowOf(emptyList<Any>()) else error(name)
        }
        val model = WidgetEditorViewModel(repository, accounts, stub<SklandRepository> { error(it) },
            stub<AttendanceStateRepository> { error(it) }, com.blueskybone.arkscreen.ui.common.TestUiStrings, CoroutineScope(coroutineContext + job))
        try { block(model, repository) } finally {
            job.cancel()
            repository.loads.forEach { (id, result) -> result.complete(WidgetConfig(id)) }
            repository.saveResult.complete(Result.success(Unit))
            job.join()
        }
    }
    private inline fun <reified T> stub(crossinline result: (String) -> Any?): T = Proxy.newProxyInstance(
        T::class.java.classLoader, arrayOf(T::class.java),
    ) { _, method, _ -> result(method.name) } as T

    private class Configs : WidgetConfigRepository {
        var loadCount = 0
        val loads = mutableMapOf<Int, CompletableDeferred<WidgetConfig>>()
        val saved = mutableListOf<WidgetConfig>()
        val saveResult = CompletableDeferred<Result<Unit>>()
        override suspend fun load(id: Int): WidgetConfig {
            loadCount++
            val pending = loads.getOrPut(id) { CompletableDeferred() }
            return withContext(NonCancellable) { pending.await() }
        }
        override suspend fun save(config: WidgetConfig) {
            saved += config
            saveResult.await().getOrThrow()
        }
        override suspend fun delete(ids: List<Int>) = Unit
        override suspend fun restore(oldIds: IntArray, newIds: IntArray) = Unit
    }
}
