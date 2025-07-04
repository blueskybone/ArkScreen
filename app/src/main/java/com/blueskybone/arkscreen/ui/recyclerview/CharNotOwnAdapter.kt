package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharNotOwnBinding
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.network.skinCachePath
import com.blueskybone.arkscreen.playerinfo.Operator
import com.blueskybone.arkscreen.ui.recyclerview.paging.PagingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

/**
 *   Created by blueskybone
 *   Date: 2025/1/20
 */
class CharNotOwnAdapter(private val context: Context, override val PAGE_SIZE: Int) :
    PagingAdapter<Operator, CharNotOwnAdapter.OperatorVH>() {

    private val profValues = context.resources.getStringArray(R.array.profession_value)
    private val profDrawable = context.resources.obtainTypedArray(R.array.profession_draw)

    private val rarityValues = context.resources.getStringArray(R.array.rarity_value)
    private val rarityDrawable = context.resources.obtainTypedArray(R.array.rarity_draw)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OperatorVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCharNotOwnBinding.inflate(inflater, parent, false)
        return OperatorVH(binding)
    }

    private fun loadImage(view: ImageView, skinId: String) {
        val file = File(skinCachePath, "$skinId.png")
        if (file.exists())
            view.load(file.absolutePath){
                crossfade(true)
                crossfade(300)
            }
        else {
            val skinUrl = URLEncoder.encode(skinId, "UTF-8")
            val url = "$avatarUrl$skinUrl.png"

            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .target { drawable: Drawable? ->
                    view.setImageDrawable(drawable)
                }
                .build()
            CoroutineScope(Dispatchers.IO).launch {
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap() // 将Drawable转换为Bitmap
                    saveImage(bitmap, skinId)
                }
            }
        }
    }

    private fun saveImage(bitmap: Bitmap, skinId: String) {
        try {
            val file = File(skinCachePath, "$skinId.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // PNG格式保存
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onBindViewHolder(holder: OperatorVH, position: Int) {
        holder.bind(currentList[position])
    }

    inner class OperatorVH(private val binding: ItemCharNotOwnBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Operator) {
//            binding.Name.text = item.name
            binding.Profession.setImageResource(
                profDrawable.getResourceId(
                    profValues.indexOf(item.profession),
                    -1
                )
            )
            val colorId = rarityValues.indexOf((item.rarity + 1).toString())
            val draw = rarityDrawable.getDrawable(colorId)
            binding.Avatar.background = draw
            loadImage(binding.Avatar, item.skinId)
        }
    }
}