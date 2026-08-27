package com.blueskybone.arkscreen.di

import com.blueskybone.arkscreen.platform.screenshot.MediaProjectionScreenshotCapturer
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotCapturer
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotNotificationFactory
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotSession
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlowStore
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotFlow
import com.blueskybone.arkscreen.ui.recruit.screenshot.RecruitScreenshotStarter
import com.blueskybone.arkscreen.ui.recruit.ocr.ImageProcessor
import com.blueskybone.arkscreen.ui.recruit.ocr.RecruitTagRecognizer
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.blueskybone.arkscreen.presentation.recruit.floating.RecruitResultDisplayer
import com.blueskybone.arkscreen.platform.tile.RecruitFloatingBallController
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
            screenshotSession = get(),
        )
    }
}

val recruitScreenshotModule = module {
    single { ImageProcessor(context = androidContext(), textTranslator = get()) }
    single { RecruitTagRecognizer(imageProcessor = get()) }
    single { FloatWindowController(application = androidContext() as android.app.Application) }
    single {
        RecruitResultDisplayer(
            context = androidContext(),
            floatWindowController = get(),
            settings = get(),
        )
    }

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
            recruitScreenshotFlow = get(),
            settings = get(),
        )
    }

    single {
        RecruitFloatingBallController(
            application = androidContext() as android.app.Application,
            starter = get(),
            floatWindowController = get(),
        )
    }
}
