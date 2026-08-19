package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.ui.account.AccountModel
import com.blueskybone.arkscreen.ui.character.CharModel
import com.blueskybone.arkscreen.ui.gacha.GachaModel
import com.blueskybone.arkscreen.ui.main.MainModel
import com.blueskybone.arkscreen.ui.realtime.RealTimeModel
import com.blueskybone.arkscreen.ui.recruit.RecruitModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AccountModel(get(), get(), get()) }
    viewModel {
        MainModel(
            repoAcc = get(),
            homeContentRepository = get(),
            remoteConfigRepository = get(),
            linkRepository = get(),
            linkMetadataResolver = get(),
            repoSkland = get(),
            checkUpdateUseCase = get(),
            startAppUpdateDownloadUseCase = get(),
            syncAccountSkUseCase = get(),
            innerPrefManager = get(),
        )
    }
    viewModel {
        GachaModel(
            repo = get(),
            repoAcc = get(),
            syncRecordsUseCase = get(),
            syncAccountGcUseCase = get(),
            backupCodec = get(),
            importDecoder = get(),
            importResolver = get(),
        )
    }
    viewModel {
        RecruitModel(
            repo = get(),
            databaseProvider = get(),
            calcResultUseCase = get(),
        )
    }
    viewModel {
        RealTimeModel(
            getRealTimeUseCase = get(),
            repo = get(),
            widgetUpdates = get(),
            syncAccountSkUseCase = get(),
            appClock = get(),
        )
    }
    viewModel {
        CharModel(
            repo = get(),
            repoAcc = get(),
            getCharAssetsUseCase = get(),
            getCharMissUseCase = get(),
            buildOperatorPosterUseCase = get(),
            settings = get(),
            syncAccountSkUseCase = get(),
            textTranslator = get(),
        )
    }
}
