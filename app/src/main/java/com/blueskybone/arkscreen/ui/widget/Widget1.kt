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

class Widget1 : AppWidgetProvider() {
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
            val views = RemoteViews(context.packageName, R.layout.widget_1x1)
            val textColor = WidgetTextColor.getColorInt(settings.widgetTextColor.get())
            val content = settings.widget1Content.get()
            val drawable = WidgetContent.getDrawableIcon(content)
            val display = formatter.format(content, compact = true)

            views.setImageViewResource(R.id.widget_bg, settings.widgetBg.get())
            views.setInt(R.id.widget_bg, "setAlpha", settings.widgetAlpha.get())
            views.setInt(R.id.value, "setTextColor", textColor)
            views.setInt(R.id.max, "setTextColor", textColor)
            views.setTextViewText(R.id.value, display.primary)
            views.setTextViewText(R.id.max, display.secondary)
            views.setImageViewResource(R.id.icon, drawable)
            views.setInt(R.id.icon, "setColorFilter", textColor)

            val sizeKey = settings.widget1Size.get()
            // A launcher cell has much less usable space after host padding than its
            // nominal size. Keep the compact widget inside that safe area.
            val mainSize = WidgetSize.getCompactTextSizeMain(sizeKey)
            val subSize = WidgetSize.getCompactTextSizeSub(sizeKey)
            val iconSize = WidgetSize.getCompactIconSize(sizeKey)
            views.setTextViewTextSize(
                R.id.value,
                TypedValue.COMPLEX_UNIT_SP,
                mainSize,
            )
            views.setTextViewTextSize(
                R.id.max,
                TypedValue.COMPLEX_UNIT_SP,
                subSize,
            )
            resizeIcon(context, views, drawable, iconSize)
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

    private fun resizeIcon(context: Context, views: RemoteViews, drawable: Int, sizeDp: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutHeight(R.id.icon, sizeDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
            views.setViewLayoutWidth(R.id.icon, sizeDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        } else {
            val size = dpToPx(sizeDp)
            val bitmap = ResourcesCompat.getDrawable(
                context.resources,
                getTargetDrawableId(drawable, settings.widgetTextColor.get()),
                null,
            )?.toBitmap() ?: return
            views.setImageViewBitmap(
                R.id.icon,
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
