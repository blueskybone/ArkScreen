package com.blueskybone.arkscreen.domain.model

/**
 * 可由远端调整的应用入口配置。默认值随安装包发布，保证离线时入口仍可使用。
 */
data class AppRemoteConfig(
    val qqGroupId: String = DEFAULT_QQ_GROUP_ID,
    val recruitDemoBvid: String = DEFAULT_RECRUIT_DEMO_BVID,
    val manualCvId: String = DEFAULT_MANUAL_CV_ID,
) {
    companion object {
        const val DEFAULT_QQ_GROUP_ID = "924153470"
        const val DEFAULT_RECRUIT_DEMO_BVID = "BV1624y1q7Cv"
        const val DEFAULT_MANUAL_CV_ID = "40623349"

        val Default = AppRemoteConfig()
    }
}
