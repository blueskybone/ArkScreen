package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.ui.account.AccountViewModel
import com.blueskybone.arkscreen.ui.character.CharViewModel
import com.blueskybone.arkscreen.ui.character.EndfieldCharViewModel
import com.blueskybone.arkscreen.ui.gacha.GachaViewModel
import com.blueskybone.arkscreen.ui.gacha.EndfieldGachaViewModel
import com.blueskybone.arkscreen.ui.main.MainViewModel
import com.blueskybone.arkscreen.ui.realtime.RealTimeViewModel
import com.blueskybone.arkscreen.ui.realtime.EndfieldRealtimeViewModel
import com.blueskybone.arkscreen.ui.recruit.RecruitViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val viewModelModule = module {
    factory<com.blueskybone.arkscreen.ui.common.UiStrings> {
        val context = androidContext()
        object : com.blueskybone.arkscreen.ui.common.UiStrings {
            override fun get(id: Int, vararg args: Any): String = context.getString(id, *args)
        }
    }
    viewModel { com.blueskybone.arkscreen.ui.widget.WidgetEditorViewModel(get(), get(), get(), get(), get()) }
    viewModel { EndfieldGachaViewModel(accounts = get(), repository = get(), texts = get()) }
    viewModel { AccountViewModel(get(), get(), get(), get()) }
    viewModel {
        MainViewModel(
            texts = get(),
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
        GachaViewModel(
            texts = get(),
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
        RecruitViewModel(
            texts = get(),
            repo = get(),
            databaseProvider = get(),
            calcResultUseCase = get(),
            settings = get(),
        )
    }
    viewModel {
        RealTimeViewModel(
            texts = get(),
            getRealTimeUseCase = get(),
            repo = get(),
            widgetUpdates = get(),
            syncAccountSkUseCase = get(),
            appClock = get(),
        )
    }
    viewModel {
        CharViewModel(
            texts = get(),
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
    viewModel { EndfieldCharViewModel(accounts = get(), skland = get(), settings = get(), texts = get()) }
    viewModel { EndfieldRealtimeViewModel(accounts = get(), skland = get(), texts = get()) }
}
