package com.blueskybone.arkscreen.ui.recyclerview.viewpager

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.databinding.ItemBannerBinding
import com.blueskybone.arkscreen.network.BiliVideo
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import timber.log.Timber


class ImagePagerAdapter(
    private val listener: ItemListener,
    private val imageList: List<BiliVideo> // 图片资源ID列表
) : RecyclerView.Adapter<ImagePagerAdapter.PagerVH>() {

    inner class PagerVH(private val binding: ItemBannerBinding, listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listener.onClick(bindingAdapterPosition)
            }
        }

        fun bind(item: BiliVideo) {
            val pic = item.pic.replace("http://", "https://")
            binding.BannerImg.load(pic){
                crossfade(true)
                crossfade(300)
            }
        }
    }

    fun getItem(position: Int): BiliVideo {
        val realPosition = position  % imageList.size // 实现无限循环
        return imageList[realPosition]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBannerBinding.inflate(inflater, parent, false)
        return PagerVH(binding, listener)
    }

    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        val realPosition = position % imageList.size // 实现无限循环
        holder.bind(imageList[realPosition])
    }

    override fun getItemCount(): Int {
        return if (imageList.size > 1) Int.MAX_VALUE else imageList.size // 单张图片不循环
    }
}