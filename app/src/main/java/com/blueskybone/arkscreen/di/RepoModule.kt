package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.data.network.RetrofitClient.akHypergryphService
import com.blueskybone.arkscreen.data.network.RetrofitClient.sklandApiService
import com.blueskybone.arkscreen.data.network.RetrofitClient.biliService
import com.blueskybone.arkscreen.data.network.RetrofitClient.hypergryphService
import com.blueskybone.arkscreen.data.repository.AccountRepositoryImpl
import com.blueskybone.arkscreen.data.repository.GachaRepositoryImpl
import com.blueskybone.arkscreen.data.repository.HomeContentRepositoryImpl
import com.blueskybone.arkscreen.data.repository.SklandRepositoryImpl
import com.blueskybone.arkscreen.data.appupdate.ApkDownloader
import com.blueskybone.arkscreen.data.appupdate.AppUpdateRemoteDataSource
import com.blueskybone.arkscreen.data.appupdate.AppUpdateRepositoryImpl
import com.blueskybone.arkscreen.data.resource.ResourceUpdateChecker
import com.blueskybone.arkscreen.data.resource.GameResourceRepositoryImpl
import com.blueskybone.arkscreen.data.resource.GameResourceStore
import com.blueskybone.arkscreen.data.resource.ResourceFileStore
import com.blueskybone.arkscreen.data.resource.ResourceJsonReader
import com.blueskybone.arkscreen.data.resource.RecruitDatabaseProviderImpl
import com.blueskybone.arkscreen.data.resource.TextTranslatorImpl
import com.blueskybone.arkscreen.data.repository.LinkRepositoryImpl
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.repository.HomeContentRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.repository.AppUpdateRepository
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.repository.LinkRepository
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider
import com.blueskybone.arkscreen.domain.service.TextTranslator
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.domain.service.ServerTimeCalibrator
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.time.PreferenceAppClock
import com.blueskybone.arkscreen.data.time.SklandServerTimeCalibrator
import com.blueskybone.arkscreen.platform.installer.ApkInstaller
import com.blueskybone.arkscreen.platform.notification.DownloadNotificationController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val repositoryModule = module {
    single<AppClock> { PreferenceAppClock(get(), get()) }
    single { HeaderProvider(get()) }
    single<ServerTimeCalibrator> {
        SklandServerTimeCalibrator(sklandApiService, get(), get())
    }
    single { ObjectMapper().registerKotlinModule() }
    single { ResourceJsonReader(objectMapper = get()) }
    single { ResourceFileStore(context = androidContext(), jsonReader = get()) }
    single {
        GameResourceStore(
            fileStore = get(),
            updateChecker = get(),
            dispatcher = get(),
        )
    }
    single<GameResourceRepository> {
        GameResourceRepositoryImpl(gameResourceStore = get(), jsonReader = get())
    }
    single<LinkRepository> { LinkRepositoryImpl(linkDao = get(), dispatcher = get()) }
    single<RecruitDatabaseProvider> { RecruitDatabaseProviderImpl(gameResourceRepository = get()) }
    single<TextTranslator> { TextTranslatorImpl(gameResourceRepository = get()) }

    single { ResourceUpdateChecker(dispatcher = get()) }
    single { AppUpdateRemoteDataSource(resourceUpdateChecker = get()) }
    single { ApkDownloader(context = androidContext(), dispatcher = get()) }
    single<AppUpdateRepository> {
        AppUpdateRepositoryImpl(remoteDataSource = get(), apkDownloader = get())
    }
    single { ApkInstaller(context = androidContext()) }
    single { DownloadNotificationController(context = androidContext()) }

    single<HomeContentRepository> {
        HomeContentRepositoryImpl(
            api = biliService,
            objectMapper = get(),
            dispatcher = get(),
        )
    }

    single<AccountRepository> {
        AccountRepositoryImpl(
            accountEfDao = get(),
            accountGcDao = get(),
            accountSkDao = get(),
            api = sklandApiService,
            apiAs = hypergryphService,
            apiAk = akHypergryphService,
            headerProvider = get(),
            preference = get()
        )
    }

    single<SklandRepository> {
        SklandRepositoryImpl(
            api = sklandApiService,
            apiAs = hypergryphService,
            headerProvider = get(),
            prefManager = get()
        )
    }

    single<GachaRepository> {
        GachaRepositoryImpl(
            gachaDao = get(),
            api = akHypergryphService,
            headerProvider = get(),
        )
    }
}
