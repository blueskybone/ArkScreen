package com.blueskybone.arkscreen.ui.main.common

import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateIndicators(position)
        }
    }

    init {
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

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


        if (videos.isEmpty()) {
            autoScrollJob?.cancel()
            bannerAdapter = null
            viewPager.adapter = null
            indicatorLayout.removeAllViews()
            return
        }

        bannerAdapter = ImagePagerAdapter(bannerListener, videos)
        viewPager.adapter = bannerAdapter
        viewPager.isUserInputEnabled = videos.size > 1
        setupIndicators(videos)
        startAutoScroll()
    }

    private val Int.dp: Int
        get() = (this * fragment.resources.displayMetrics.density).toInt()

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
        val indicatorCount = indicatorLayout.childCount
        if (indicatorCount == 0) return
        val selectedIndex = position % indicatorCount
        for (i in 0 until indicatorCount) {
            (indicatorLayout.getChildAt(i) as ImageView).setImageResource(
                if (i == selectedIndex) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    delay(5000)
                    val count = viewPager.adapter?.itemCount ?: 0
                    if (count > 1) {
                        val nextItem = (viewPager.currentItem + 1) % count
                        viewPager.setCurrentItem(nextItem, true)
                    }
                }
            }
        }
    }

    fun release() {
        autoScrollJob?.cancel()
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        viewPager.adapter = null
        bannerAdapter = null
    }

}
