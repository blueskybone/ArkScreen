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
import com.blueskybone.arkscreen.domain.usecase.operator.BuildOperatorPosterUseCase
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CheckAppUpdateUseCase(get(), get()) }
    factory { DownloadAppUpdateUseCase(get()) }
    factory { StartAppUpdateDownloadUseCase(get()) }

    // 账号
    factory { LoadCookieUseCase(get()) }
    factory { SyncAccountGcUseCase(get()) }
    factory { SyncAccountSkUseCase(get()) }

    // 签到
    factory { GetAttdResultUseCase(get()) }
    factory { RunAttendanceUseCase(get(), get()) }

    // 寻访
    factory { SyncRecordsUseCase(get()) }

    // 干员
    factory { GetCharAssetsUseCase(get()) }
    factory { GetCharMissUseCase(get()) }
    factory { BuildOperatorPosterUseCase() }

    // 实时数据
    factory { GetRealTimeUseCase(get()) }

    // 公招
    factory { CalcResultUseCase(get()) }
}
