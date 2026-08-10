package com.blueskybone.arkscreen.domain.repository


import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.cache.CacheAccountInfo
import com.blueskybone.arkscreen.domain.model.cache.LaborCache
import com.blueskybone.arkscreen.domain.model.cache.MeetCache
import com.blueskybone.arkscreen.domain.model.cache.RecruitCache
import com.blueskybone.arkscreen.domain.model.cache.RefreshCache
import com.blueskybone.arkscreen.domain.model.cache.TrainCache
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.model.operator.OperatorAssets
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData


/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
interface SklandRepository {

    suspend fun fetchRealTimeData(account: AccountSk): Result<RealTimeData>  //解析实时数据

    suspend fun fetchCharAssets(account: AccountSk): Result<OperatorAssets>    //解析干员资产

    suspend fun fetchAkCheckResult(account: AccountSk): Result<String> //明日方舟签到结果

    suspend fun fetchEfCheckResult(account: AccountEf): Result<String> //终末地签到结果

    fun setRealTimeCache(account: AccountSk, realTimeData: RealTimeData)

    fun getApCache(): ApCache

    fun getCacheAccountInfo(): CacheAccountInfo

    fun getTrainCache(): TrainCache

    fun getLaborCache(): LaborCache

    fun getRecruitCache(): RecruitCache

    fun getRefreshCache(): RefreshCache

    fun getMeetCache(): MeetCache

}
