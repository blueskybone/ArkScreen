package com.blueskybone.arkscreen.ui

/**
 * 页面或操作的持久状态，由 ViewModel 持有，界面在 STARTED 生命周期内观察并渲染。
 *
 * 使用规则：
 * - 同一次加载使用一个 status，不同时维护可独立写入的 loading/error/success。
 * - loading/error 等兼容属性只能从 status 派生；页面数据和筛选条件仍放在各自 UiState。
 * - Loading.message 承载动态进度，直接显示在加载动画下方；空值由页面提供资源文案。
 * - Toast、导航等一次性动作走事件流，不把 Success 当作需要重复触发的事件。
 * - 持久偏好由设置存储提供唯一来源，ViewModel 观察和修改；Bundle 不再重复保存该偏好。
 * - 即时生效设置使用 PreferenceBinder；有“保存”操作的表单保留独立草稿，提交时才写入。
 *
 * 组件规则：信息使用 MetadataCapsule，操作/筛选使用 ActionChip/FilterChip；公招保留专用
 * 派生样式。动态创建的通用筛选项同样 inflate XML 样式，不在 Kotlin 中另写颜色和尺寸。
 * 分区标题复用 PageSectionTitle，账号选择行复用 account_summary_selector。
 *
 * 既有 LiveData 页面可以继续使用 LiveData，统一的是所有权和生命周期规则，而非容器名称。
 */
sealed interface UiStatus {
    data object Idle : UiStatus
    data class Loading(val message: String? = null) : UiStatus
    data class Success(val message: String? = null) : UiStatus
    data class Empty(val message: String) : UiStatus
    data class Error(val message: String) : UiStatus
}
