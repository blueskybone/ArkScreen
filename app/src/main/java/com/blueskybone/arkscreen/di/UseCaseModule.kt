package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.appupdate.DownloadAppUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.appupdate.StartAppUpdateDownloadUseCase
import com.blueskybone.arkscreen.domain.usecase.account.LoadCookieUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountGcUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.domain.usecase.attendance.GetAttdResultUseCase
import com.blueskybone.arkscreen.domain.usecase.attendance.RunAttendanceUseCase
import com.blueskybone.arkscreen.domain.usecase.gacha.SyncRecordsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharAssetsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharMissUseCase
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CheckAppUpdateUseCase(get(), get()) }
    factory { DownloadAppUpdateUseCase(get()) }
    factory { StartAppUpdateDownloadUseCase(get()) }

    // Account
    factory { LoadCookieUseCase(get()) }
    factory { SyncAccountGcUseCase(get()) }
    factory { SyncAccountSkUseCase(get()) }

    // Attendance
    factory { GetAttdResultUseCase(get()) }
    factory { RunAttendanceUseCase(get(), get()) }

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
