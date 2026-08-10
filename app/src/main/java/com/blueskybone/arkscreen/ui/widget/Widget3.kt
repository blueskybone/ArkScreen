package com.blueskybone.arkscreen.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.util.dpToPx
import org.koin.java.KoinJavaComponent.getKoin

class Widget3 : AppWidgetProvider() {
    private val settings: SettingPrefManager by getKoin().inject()
    private val cache: CachePrefManager by getKoin().inject()
    private val appClock: AppClock by getKoin().inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val formatter = WidgetContentFormatter(cache, appClock)
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_2x2)
            val textColor = WidgetTextColor.getColorInt(settings.widgetTextColor.get())
            val sizeKey = settings.widget3Size.get()

            views.setImageViewResource(R.id.widget_bg, settings.widgetBg.get())
            views.setInt(R.id.widget_bg, "setAlpha", settings.widgetAlpha.get())
            bindRow(
                context = context,
                views = views,
                content = settings.widget3Content1.get(),
                textId = R.id.text_1,
                restId = R.id.rest_1,
                iconId = R.id.icon_1,
                textColor = textColor,
                sizeKey = sizeKey,
                formatter = formatter,
            )
            bindRow(
                context = context,
                views = views,
                content = settings.widget3Content2.get(),
                textId = R.id.text_2,
                restId = R.id.rest_2,
                iconId = R.id.icon_2,
                textColor = textColor,
                sizeKey = sizeKey,
                formatter = formatter,
            )
            views.setOnClickPendingIntent(R.id.layout, refreshIntent(context, appWidgetId))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context?) {
        context?.let(WidgetWorkScheduler::onWidgetEnabled)
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        context?.let(WidgetWorkScheduler::cancelIfNoWidgets)
        super.onDisabled(context)
    }

    private fun bindRow(
        context: Context,
        views: RemoteViews,
        content: String,
        textId: Int,
        restId: Int,
        iconId: Int,
        textColor: Int,
        sizeKey: String,
        formatter: WidgetContentFormatter,
    ) {
        val drawable = WidgetContent.getDrawableIcon(content)
        val display = formatter.format(content)
        views.setTextViewText(textId, display.primary)
        views.setTextViewText(restId, display.secondary)
        views.setInt(textId, "setTextColor", textColor)
        views.setInt(restId, "setTextColor", textColor)
        views.setImageViewResource(iconId, drawable)
        views.setInt(iconId, "setColorFilter", textColor)
        views.setTextViewTextSize(
            textId,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSizeMain(sizeKey),
        )
        views.setTextViewTextSize(
            restId,
            TypedValue.COMPLEX_UNIT_SP,
            WidgetSize.getTextSizeSub(sizeKey),
        )
        resizeIcon(context, views, drawable, iconId, WidgetSize.getImageSize(sizeKey))
    }

    private fun resizeIcon(
        context: Context,
        views: RemoteViews,
        drawable: Int,
        iconId: Int,
        sizeDp: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutHeight(iconId, sizeDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
            views.setViewLayoutWidth(iconId, sizeDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        } else {
            val size = dpToPx(sizeDp)
            val bitmap = ResourcesCompat.getDrawable(
                context.resources,
                getTargetDrawableId(drawable, settings.widgetTextColor.get()),
                null,
            )?.toBitmap() ?: return
            views.setImageViewBitmap(
                iconId,
                Bitmap.createScaledBitmap(bitmap, size, size, true),
            )
        }
    }

    private fun refreshIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, WidgetReceiver::class.java).apply {
            action = WidgetReceiver.MANUAL_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
