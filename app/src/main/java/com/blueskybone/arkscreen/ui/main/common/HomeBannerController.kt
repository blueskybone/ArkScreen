package com.blueskybone.arkscreen.ui.main.common

import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.ui.common.adapter.ItemListener
import com.blueskybone.arkscreen.ui.common.adapter.viewpager.ImagePagerAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by blueskybone
 * Date: 2026/4/2
 */
class HomeBannerController(
    private val fragment: Fragment,
    private val viewPager: ViewPager2,
    private val indicatorLayout: LinearLayout,

    ) {

    private var autoScrollJob: Job? = null
    private var videos: List<BiliVideo> = emptyList()

    private var bannerAdapter: ImagePagerAdapter? = null

    private val bannerListener = object : ItemListener {
        override fun onClick(position: Int) {
            bannerAdapter?.getItem(position)?.let { value ->
                try {
                    val intent =
                        Intent(Intent.ACTION_VIEW, "bilibili://video/${value.bVid}".toUri())
                    intent.setPackage("tv.danmaku.bili")
                    fragment.startActivity(intent)
                } catch (_: Exception) {
                    fragment.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://bilibili.com/video/${value.bVid}".toUri()
                        )
                    )
                }
            }
        }

        override fun onLongClick(position: Int) = Unit
    }

    fun submitList(videos: List<BiliVideo>) {
        if (this.videos == videos) return
        this.videos = videos


        // 如果数据为空，直接返回
        if (videos.isEmpty()) return

        // 设置 ViewPager adapter

//        if (bannerAdapter == null) {
//            bannerAdapter = ImagePagerAdapter(bannerListener, videos)
//            binding.TitleBanner.adapter = bannerAdapter
//            binding.TitleBanner.isUserInputEnabled = true
//            setupIndicators(videos)
//            startAutoScroll()
//        } else {
//            bannerAdapter = ImagePagerAdapter(bannerListener, videos)
//            binding.TitleBanner.adapter = bannerAdapter
//            setupIndicators(videos)
//        }

        val bannerAdapter = ImagePagerAdapter(bannerListener, videos)
        viewPager.adapter = bannerAdapter
        viewPager.isUserInputEnabled = true // 可滑动

        // 设置指示器
        setupIndicators(videos)

        // 开始自动轮播
        startAutoScroll()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private fun setupIndicators(videos: List<BiliVideo>) {
        indicatorLayout.removeAllViews()

        videos.forEach { _ ->
            val indicator = ImageView(fragment.requireContext()).apply {
                setImageResource(R.drawable.dot_unselected)
                layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp).apply {
                    setMargins(8.dp, 0, 8.dp, 0)
                }
            }
            indicatorLayout.addView(indicator)
        }

        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            (indicatorLayout.getChildAt(i) as ImageView).setImageResource(
                if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(5000) // 每5秒切换一次
                val count = viewPager.adapter?.itemCount ?: 0
                if (count > 1) {
                    val nextItem = (viewPager.currentItem + 1) % count
                    viewPager.setCurrentItem(nextItem, true)
                    updateIndicators(nextItem)
                }
            }
        }
    }

    fun release() {
        autoScrollJob?.cancel() // 取消自动轮播
    }

}