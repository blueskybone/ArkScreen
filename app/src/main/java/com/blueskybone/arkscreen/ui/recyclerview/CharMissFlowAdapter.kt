package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharNotOwnBinding
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.network.skinCachePath
import com.blueskybone.arkscreen.playerinfo.Operator
import com.nex3z.flowlayout.FlowLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

class CharMissFlowAdapter(
    private val context: Context,
    private val flowLayout: FlowLayout
) {
    private val profValues = context.resources.getStringArray(R.array.profession_value)
    private val profDrawable = context.resources.obtainTypedArray(R.array.profession_draw)
    private val rarityValues = context.resources.getStringArray(R.array.rarity_value)
    private val rarityDrawable = context.resources.obtainTypedArray(R.array.rarity_draw)

    private var operators: List<Operator> = emptyList()

    fun submitList(newList: List<Operator>) {
        operators = newList
        flowLayout.removeAllViews()
        newList.forEach { operator ->
            addOperatorView(operator)
        }
    }

    private fun addOperatorView(operator: Operator) {

        val binding = ItemCharNotOwnBinding.inflate(
            LayoutInflater.from(context),
            flowLayout,
            false
        )
        bindView(binding, operator)
    }

    private fun bindView(binding: ItemCharNotOwnBinding, item: Operator) {
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

    private fun loadImage(view: ImageView, skinId: String) {
        val skinUrl = URLEncoder.encode(skinId, "UTF-8")
        val url = "$avatarUrl$skinUrl.png"

        view.load(url){

        }
    }


    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density + 0.5f).toInt()
}