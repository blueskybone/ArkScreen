package com.blueskybone.arkscreen.domain.usecase.operator

import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator

data class OperatorPoster(
    val accountName: String,
    val serverName: String,
    val accountAvatarUrl: String?,
    val rows: List<ProfessionRow>,
) {
    val operatorCount: Int = rows.sumOf { it.operators.size }

    data class ProfessionRow(
        val profession: String,
        val professionName: String,
        val operators: List<Operator>,
    )
}

class BuildOperatorPosterUseCase {

    operator fun invoke(
        account: AccountSk,
        operators: List<Operator>,
        accountAvatarUrl: String? = null,
    ): OperatorPoster {
        val eligible = operators.filter { it.rarity == SIX_STAR && it.evolvePhase == ELITE_TWO }
        return OperatorPoster(
            accountName = account.nickName,
            serverName = if (account.official) "官服" else "B服",
            accountAvatarUrl = accountAvatarUrl,
            rows = PROFESSIONS.map { (profession, name) ->
                OperatorPoster.ProfessionRow(
                    profession = profession,
                    professionName = name,
                    operators = eligible
                        .filter { it.profession == profession }
                        .sortedWith(operatorComparator),
                )
            },
        )
    }

    private companion object {
        const val SIX_STAR = 5
        const val ELITE_TWO = 2

        val PROFESSIONS = listOf(
            "PIONEER" to "先锋",
            "WARRIOR" to "近卫",
            "TANK" to "重装",
            "SNIPER" to "狙击",
            "CASTER" to "术师",
            "MEDIC" to "医疗",
            "SUPPORT" to "辅助",
            "SPECIAL" to "特种",
        )

        val operatorComparator =
            compareByDescending<Operator> { it.level }
                .thenByDescending { it.potentialRank }
                .thenByDescending { operator ->
                    operator.skills.count { it.specializeLevel == 3 }
                }
                .thenByDescending { operator ->
                    operator.equips.count { !it.locked && it.stage == 3 }
                }
                .thenBy { it.name }
    }
}
