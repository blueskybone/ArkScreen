package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.repository.EndfieldGachaRepository
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaPool
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import com.blueskybone.arkscreen.ui.UiStatus
import org.junit.Test
import java.lang.reflect.Proxy

class EndfieldGachaViewModelTest {
    @Test fun cancelledRequestCannotReplaceNewAccountsLoadingState() = runBlocking {
        fixture { model, source ->
            val old = source.requests.single()
            model.select("B")
            drain()
            old.finish.complete(Result.failure(CancellationException("old request cancelled")))
            drain()
            assertEquals("B", model.state.value.selectedUid)
            assertTrue(model.state.value.loading)
            assertNull(model.state.value.error)
        }
    }

    @Test fun lateProgressAndFailureAreIgnoredAfterSwitch() = runBlocking {
        fixture { model, source ->
            val old = source.requests.single()
            model.select("B")
            drain()
            source.requests.last().progress("B progress")
            old.progress("A stale progress")
            old.finish.complete(Result.failure(IllegalStateException("A failed")))
            drain()
            assertEquals(UiStatus.Loading("B progress"), model.state.value.status)
            assertEquals("B progress", model.state.value.progress)
            assertTrue(model.state.value.loading)
            assertNull(model.state.value.error)
        }
    }

    @Test fun switchingBackToSameAccountStillRejectsFirstRequestsCompletion() = runBlocking {
        fixture { model, source ->
            val first = source.requests.single()
            model.select("B")
            drain()
            model.select("A")
            drain()
            first.progress("first A progress")
            first.finish.complete(Result.success(0))
            drain()
            assertEquals("A", model.state.value.selectedUid)
            assertTrue(model.state.value.loading)
            assertEquals("正在授权…", model.state.value.progress)
            assertEquals(3, source.requests.size)
        }
    }

    @Test fun currentFailureAllowsRetryAndCompletionRejectsLateProgress() = runBlocking {
        fixture { model, source ->
            source.requests.single().finish.complete(Result.failure(IllegalStateException("offline")))
            drain()
            assertFalse(model.state.value.loading)
            assertEquals(UiStatus.Error("offline"), model.state.value.status)
            assertEquals("offline", model.state.value.error)
            model.refresh()
            model.refresh()
            drain()
            assertEquals(2, source.requests.size)
            val retry = source.requests.last()
            retry.finish.complete(Result.success(1))
            drain()
            retry.progress("late progress")
            drain()
            assertFalse(model.state.value.loading)
            assertNull(model.state.value.error)
            assertEquals(UiStatus.Success("同步完成"), model.state.value.status)
            assertEquals("同步完成", model.state.value.progress)
        }
    }

    @Test fun filtersAndProgressReuseSharedSummary() = runBlocking {
        fixture { model, source ->
            val initial = withTimeout(3000) { model.pageData.filterNotNull().first { it.summary.input.uid == "A" } }
            model.setRecordFilter(EndfieldRecordFilter(sixOnly = true))
            val filtered = withTimeout(3000) { model.pageData.filterNotNull().first { it.filter.sixOnly } }
            assertSame(initial.summary, filtered.summary)
            source.requests.single().progress("next page")
            drain()
            assertSame(filtered, model.pageData.value)
            model.selectCategory("weapon")
            val weapon = withTimeout(3000) { model.pageData.filterNotNull().first { it.summary.input.type == "weapon" } }
            assertEquals("A", weapon.summary.input.uid)
            assertTrue(weapon.filter.sixOnly)
        }
    }

    private suspend fun drain() { repeat(8) { yield() } }

    private suspend fun CoroutineScope.fixture(block: suspend (EndfieldGachaViewModel, Source) -> Unit) {
        val a = account("A")
        val b = account("B")
        val current = MutableStateFlow<AccountEf?>(a)
        val repository = Proxy.newProxyInstance(
            AccountRepository::class.java.classLoader, arrayOf(AccountRepository::class.java),
        ) { _, method, args ->
            when (method.name.substringBefore('-')) {
                "observeEfAcc" -> MutableStateFlow(listOf(a, b))
                "observeCurrentEfAcc" -> current
                "setCurrentAccountEf" -> { current.value = args!![0] as AccountEf; Unit }
                else -> error("Unexpected call: ${method.name}")
            }
        } as AccountRepository
        val job = SupervisorJob(coroutineContext[Job])
        val scope = CoroutineScope(coroutineContext + job)
        val source = Source()
        val model = EndfieldGachaViewModel(repository, source, com.blueskybone.arkscreen.ui.common.TestUiStrings, scope)
        try {
            drain()
            block(model, source)
        } finally {
            job.cancel()
            source.requests.forEach { it.finish.complete(Result.success(0)) }
            job.join()
        }
    }

    private fun account(id: String) = AccountEf(id, id, "", true, "", "", id, "1")
    private class Request(val progress: (String) -> Unit) {
        val finish = CompletableDeferred<Result<Int>>()
    }
    private class Source : EndfieldGachaRepository {
        val requests = mutableListOf<Request>()
        override fun observe(roleId: String): Flow<List<EndfieldGachaRecord>> = flowOf(emptyList())
        override fun observePools(roleId: String): Flow<List<EndfieldGachaPool>> = flowOf(emptyList())
        override suspend fun sync(account: AccountEf, progress: (String) -> Unit): Int {
            val request = Request(progress).also(requests::add)
            // Simulate a transport whose callback arrives even after cancellation.
            return withContext(NonCancellable) { request.finish.await().getOrThrow() }
        }
    }
}
