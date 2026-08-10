package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.data.network.RetrofitClient.akHypergryphService
import com.blueskybone.arkscreen.data.network.RetrofitClient.sklandApiService
import com.blueskybone.arkscreen.data.network.RetrofitClient.biliService
import com.blueskybone.arkscreen.data.network.RetrofitClient.hypergryphService
import com.blueskybone.arkscreen.data.repository.AccountRepositoryImpl
import com.blueskybone.arkscreen.data.repository.GachaRepositoryImpl
import com.blueskybone.arkscreen.data.repository.ResourceRepositoryImpl
import com.blueskybone.arkscreen.data.repository.SklandRepositoryImpl
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ResourceRepository> {
        ResourceRepositoryImpl(
            linkDao = get(),
            context = APP,
            dispatcher = get(),
            api = biliService
        )
    }

    single<AccountRepository> {
        AccountRepositoryImpl(
            accountEfDao = get(),
            accountGcDao = get(),
            accountSkDao = get(),
            api = sklandApiService,
            apiAk = hypergryphService,
            preference = get()
        )
    }

    single<SklandRepository> {
        SklandRepositoryImpl(
            api = sklandApiService,
            prefManager = get()
        )
    }

    single<GachaRepository> {
        GachaRepositoryImpl(
            gachaDao = get(),
            api = akHypergryphService
        )
    }
}