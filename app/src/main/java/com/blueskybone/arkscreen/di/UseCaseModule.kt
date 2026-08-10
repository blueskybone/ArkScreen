package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.domain.usecase.CheckUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.DownloadAppUseCase
import com.blueskybone.arkscreen.domain.usecase.account.LoadCookieUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountGcUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.domain.usecase.attendance.GetAttdResultUseCase
import com.blueskybone.arkscreen.domain.usecase.gacha.SyncRecordsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharAssetsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharMissUseCase
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CheckUpdateUseCase(get(), get()) }
    factory { DownloadAppUseCase(get()) }

    // Account
    factory { LoadCookieUseCase(get()) }
    factory { SyncAccountGcUseCase(get()) }
    factory { SyncAccountSkUseCase(get()) }

    // Attendance
    factory { GetAttdResultUseCase(get()) }

    // Gacha
    factory { SyncRecordsUseCase(get()) }

    // Operator
    factory { GetCharAssetsUseCase(get()) }
    factory { GetCharMissUseCase(get()) }

    // RealTime
    factory { GetRealTimeUseCase(get()) }

    // Recruit
    factory { CalcResultUseCase(get()) }
}
