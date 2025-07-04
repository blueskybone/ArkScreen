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

    private val imageLoader = ImageLoader(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var operators: List<Operator> = emptyList()

//    init {
//        // 回收资源
//        flowLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(v: View) {}
//            override fun onViewDetachedFromWindow(v: View) {
//                scope.cancel()
//                profDrawable.recycle()
//                rarityDrawable.recycle()
//            }
//        })
//    }

    fun submitList(newList: List<Operator>) {
        operators = newList
        flowLayout.removeAllViews()
        newList.forEach { operator ->
            addOperatorView(operator)
        }
    }

    private fun addOperatorView(operator: Operator) {
        // 动态加载布局
        val binding = ItemCharNotOwnBinding.inflate(
            LayoutInflater.from(context),
            flowLayout,
            false
        )

        // 绑定数据
        bindView(binding, operator)

        // 添加到FlowLayout
        flowLayout.addView(binding.root.apply {
            // 设置布局参数
//            layoutParams = FlowLayout.LayoutParams(
//                FlowLayout.LayoutParams.WRAP_CONTENT,
//                FlowLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                setMargins(8.dp, 8.dp, 8.dp, 8.dp)
//            }
        })
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
            crossfade(true)
            crossfade(300)
        }


//        val file = File(skinCachePath, "$skinId.png")
//        if (file.exists()) {
//            view.load(file.absolutePath)
//        } else {
//            val skinUrl = URLEncoder.encode(skinId, "UTF-8")
//            val url = "$avatarUrl$skinUrl.png"
//
//            val request = ImageRequest.Builder(context)
//                .data(url)
//                .target { drawable ->
//                    view.setImageDrawable(drawable)
//                    drawable.toBitmap().let { bitmap ->
//                        scope.launch(Dispatchers.IO) {
//                            saveImage(bitmap, skinId)
//                        }
//                    }
//                }
//                .build()
//
//            imageLoader.enqueue(request)
//        }
    }

    private fun saveImage(bitmap: Bitmap, skinId: String) {
        try {
            val file = File(skinCachePath, "$skinId.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density + 0.5f).toInt()
}