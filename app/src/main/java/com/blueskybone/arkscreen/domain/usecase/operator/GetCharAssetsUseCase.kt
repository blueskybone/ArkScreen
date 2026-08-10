package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.repository.SklandRepository

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
class GetCharAssetsUseCase(private val repo: SklandRepository) {
    suspend operator fun invoke(account: AccountSk): Result<List<Operator>> =
        repo.fetchCharAssets(account).map { operators ->
            operators.sortedWith(OperatorOrdering.default)
        }
}
