package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsBinding
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.playerinfo.Records
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import java.net.URLEncoder


/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */
class RecordsAdapter(private val context: Context) :
    ListAdapter<Records, RecordsAdapter.RecordsVH>(DiffCallback) {


    private object DiffCallback : DiffUtil.ItemCallback<Records>() {
        override fun areItemsTheSame(oldItem: Records, newItem: Records): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Records, newItem: Records): Boolean {
            return (oldItem.charId == newItem.charId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGachaRecordsBinding.inflate(inflater, parent, false)
        return RecordsVH(binding)
    }

    override fun onBindViewHolder(holder: RecordsVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordsVH(private val binding: ItemGachaRecordsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Records) {
            binding.GachaCount.text = context.getString(R.string.gacha_count, item.count)
            binding.ProgressBar.progress = item.count
            binding.GainDate.text = getTimeStr(item.ts, "MM-dd")
            if (item.isNew) {
                binding.IsNew.visibility = View.VISIBLE
            } else {
                binding.IsNew.visibility = View.GONE
            }

//            val layerDrawable = binding.ProgressBar.progressDrawable as LayerDrawable
//            val progressDrawable1 =
//                layerDrawable.findDrawableByLayerId(android.R.id.progress) as ScaleDrawable
//            val progressDrawable = progressDrawable1.drawable as GradientDrawable
//            progressDrawable.setColor(Color.RED) // 设置进度条颜色
//            if (item.count < 37) {
//                progressDrawable.setColor(ContextCompat.getColor(context, R.color.trans_blue))
//            } else if (item.count < 50) {
//                progressDrawable.setColor(ContextCompat.getColor(context, R.color.trans_blue))
//            } else {
//                progressDrawable.setColor(ContextCompat.getColor(context, R.color.trans_blue))
//            }

            val skinUrl = URLEncoder.encode(item.charId + "#1.png", "UTF-8")
            val url = "$avatarUrl$skinUrl"
            binding.Avatar.load(url)
        }
    }
}