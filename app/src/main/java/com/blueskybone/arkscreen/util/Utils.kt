package com.blueskybone.arkscreen.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.CookieManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.BuildConfig
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.ui.activity.WebViewActivity
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder


fun getRealScreenSize(context: Context): Point {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = wm.currentWindowMetrics.bounds
        Point(
            bounds.width(),
            bounds.height()
        )
    } else {
        Point().apply {
            @Suppress("DEPRECATED")
            wm.defaultDisplay.getSize(this)
        }
    }
}


fun getAppVersionName(context: Context): String {
//    var versionname: String? = null
//    val pm = context.packageManager
//    try {
//        val packageInfo = pm.getPackageInfo(context.packageName, 0)
//        versionname = packageInfo.versionName
//    } catch (e: PackageManager.NameNotFoundException) {
//        e.printStackTrace()
//    }
//    return versionname
    return BuildConfig.VERSION_NAME
}

fun getJsonContent(jsonStr: String?, key: String): String {
    try {
        val om = ObjectMapper()
        val tree = om.readTree(jsonStr)
        val keys = tree.findValues(key)
        return keys[0].asText()
    } catch (e: Exception) {
        throw Exception("try get json content failed: content: $jsonStr , key: $key")
    }
}

fun readFileAsJsonNode(path: String): JsonNode {
    val inputStream = FileInputStream(path)
    val om = ObjectMapper()
    return om.readTree(inputStream)
}

fun dpToPx(context: Context, dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
}

fun dpToPx(dp: Int): Int {
    return (dp * APP.resources.displayMetrics.density).toInt()
}

fun spToPx(context: Context, sp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        context.resources.displayMetrics
    )
}

fun getColorFromAttr(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data // 返回颜色值
}

fun getDensityDpi(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val displayMetrics = DisplayMetrics()
        context.resources.displayMetrics?.let {
            displayMetrics.setTo(it)
        }
        displayMetrics.densityDpi
    } else {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        metrics.densityDpi
    }
}


//TODO: 改一下逻辑，给ConfigRes复用
fun getAssetsFilepath(filename: String): String {
    val context = APP
    val filePath = "${context.externalCacheDir.toString()}/${filename}"
    val cacheFile = File(context.externalCacheDir, filename)
    return try {
        if (cacheFile.exists()) {
            filePath
        } else {
            try {
                context.assets.open(filename).use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        val buf = ByteArray(4096)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) {
                            outputStream.write(buf, 0, len)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            cacheFile.absolutePath
        }
    } catch (e: Exception) {
        filePath
    }
}

fun getEleCombination(list: List<String>, range: Int = 3): List<List<String>> {
    val combList = mutableListOf<List<String>>()
    for (idx in range downTo 1) {
        combList.addAll(getOneCombination(list, idx))
    }
    return combList
}

fun getOneCombination(list: List<String>, range: Int): List<List<String>> {
    val comList: MutableList<List<String>> = ArrayList()
    val size = list.size
    //TODO：修改上限
    for (str in (1 shl size) - 1 downTo 0) {
        var cnt = 0
        val array = IntArray(10)
        val strList: MutableList<String> = ArrayList()
        for (i in 0 until size) {
            if (str and (1 shl i) != 0) {
                array[cnt++] = i
            }
        }
        if (cnt == range) {
            for (i in range - 1 downTo 0) {
                strList.add(list[size - 1 - array[i]])
            }
            comList.add(strList)
        }
    }
    return comList
}

fun getCookie(url: String, name: String): String {
    val cookieManager = CookieManager.getInstance()
    val cookieStr = cookieManager.getCookie(url)
    val head = "$name="
    val tail = ";"
    val idx1 = cookieStr.indexOf(head)
    val idx2 = cookieStr.indexOf(tail, idx1)
    val tokenEncoded = cookieStr.substring(idx1 + head.length, idx2)
    return URLDecoder.decode(tokenEncoded, "UTF-8")
}

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clipData)
    Toaster.show(getString(context, com.blueskybone.arkscreen.R.string.copied))
}

fun updateNotification(
    context: Context,
    title: String,
    message: String,
    channelId: String,
    channelName: String
) {
    // 创建通知渠道（适用于 Android 8.0 及以上）
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 1
    val channel =
        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
    notificationManager.createNotificationChannel(channel)
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_notification)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
    notificationManager.notify(notificationId, notificationBuilder.build())
}


fun openLink(context: Context, url: String, prefManager: PrefManager) {
    if (prefManager.useInnerWeb.get()) {
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("url", url)
        context.startActivity(intent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}