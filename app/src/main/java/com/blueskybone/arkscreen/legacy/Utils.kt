package com.blueskybone.arkscreen.legacy

//@Deprecated("重构结束后删除")
//suspend fun doSklandAttendance(context: Context) {
//    val database = ArkDatabase.getDatabase(APP)
//    val accountSkDao = database.getAccountSkDao()
//    val accountList = accountSkDao.getAll()
//    val channelId = "atd_notify_channel"
//    val channelName = "签到通知"
//
//    val accountEfList = database.getAccountEfDao().getAll()
//    val size = accountEfList.size + accountList.size
//    for ((idx, account) in accountList.withIndex()) {
//        updateNotification(
//            context,
//            "正在签到中 (${idx + 1}/${accountList.size})",
//            account.nickName,
//            channelId,
//            channelName
//        )
//        val msg = sklandAttendance(account)
//        Timber.i(account.nickName + " : " + msg)
//        updateNotification(
//            context,
//            "正在签到中 (${idx + 1}/${accountList.size})",
//            account.nickName + " : " + msg,
//            channelId,
//            channelName
//        )
//        withContext(Dispatchers.IO) {
//            Thread.sleep(500)
//        }
//    }
//
//    for ((idx, account) in accountEfList.withIndex()) {
//        updateNotification(
//            context,
//            "正在签到中 (${idx + 1}/${accountEfList.size})",
//            account.nickName,
//            channelId,
//            channelName
//        )
//        val msg = endfieldAttendance(account)
//        Timber.i(account.nickName + " : " + msg)
//        updateNotification(
//            context,
//            "正在签到中 (${idx + 1}/${accountEfList.size})",
//            account.nickName + " : " + msg,
//            channelId,
//            channelName
//        )
//        withContext(Dispatchers.IO) {
//            Thread.sleep(500)
//        }
//    }
//
//
//    updateNotification(
//        context,
//        "签到完成 (${size}/${size})",
//        "",
//        channelId,
//        channelName
//    )
//}