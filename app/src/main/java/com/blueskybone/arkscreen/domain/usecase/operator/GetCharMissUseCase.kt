package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.common.safeResultSync
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository


/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

class GetCharMissUseCase(
    private val repo: GameResourceRepository,
) {

    suspend operator fun invoke(
        charOwnList: List<Operator>,
    ): Result<List<Operator>> = safeResultSync {
        val ownCharIds = charOwnList
            .map { it.charId }
            .toSet()

        val allOperators = repo.getOperatorBasicInfoMap()
            .getOrThrow()

        allOperators
            .filterKeys { charId -> charId !in ownCharIds }
            .values
            .map { info ->
                Operator().apply {
                    charId = info.charId
                    skinId = info.skinId
                    name = info.name
                    rarity = info.rarity
                    profession = info.profession
                }
            }
            .sortedWith(OperatorOrdering.default)
    }
}
