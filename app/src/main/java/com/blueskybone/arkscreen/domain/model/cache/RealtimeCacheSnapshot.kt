package com.blueskybone.arkscreen.domain.model.cache

/** Only the small set of values needed by home and widgets; no raw player response. */
data class RealtimeCacheSnapshot(
    val accountInfo: CacheAccountInfo = CacheAccountInfo.empty(),
    val apCache: ApCache = ApCache.default(),
    val laborCache: LaborCache = LaborCache.default(),
    val trainCache: TrainCache = TrainCache.default(),
    val recruitCache: RecruitCache = RecruitCache.default(),
    val refreshCache: RefreshCache = RefreshCache.default(),
    val meetCache: MeetCache = MeetCache.default(),
)
