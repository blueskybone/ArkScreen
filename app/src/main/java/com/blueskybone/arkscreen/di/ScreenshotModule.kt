package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.platform.screenshot.MediaProjectionScreenshotCapturer
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCapturer
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotNotificationFactory
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotSession
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlowStore
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotFlow
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotStarter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val screenshotModule = module {

    single {
        ScreenshotSession()
    }

    single {
        ScreenshotTaskFlowStore()
    }

    single {
        ScreenshotNotificationFactory(
            context = androidContext()
        )
    }

    single<ScreenshotCapturer> {
        MediaProjectionScreenshotCapturer(
            context = androidContext(),
            screenshotSession = get()
        )
    }
}

val recruitScreenshotModule = module {

    single {
        RecruitScreenshotFlow(
            tagRecognizer = get(),
            calcRecruitResultUseCase = get(),
            resultDisplayer = get()
        )
    }

    single {
        RecruitScreenshotStarter(
            screenshotSession = get(),
            taskFlowStore = get(),
            recruitScreenshotFlow = get()
        )
    }
}