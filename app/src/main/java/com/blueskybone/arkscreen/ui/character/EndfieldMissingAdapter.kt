package com.blueskybone.arkscreen.ui.character

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharGridBinding
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo

internal class EndfieldMissingAdapter(private val onClick: (EndfieldOperatorInfo) -> Unit) :
    ListAdapter<EndfieldOperatorInfo, EndfieldMissingAdapter.Holder>(object : DiffUtil.ItemCallback<EndfieldOperatorInfo>() {
        override fun areItemsTheSame(old: EndfieldOperatorInfo, new: EndfieldOperatorInfo) = old.wikiId == new.wikiId
        override fun areContentsTheSame(old: EndfieldOperatorInfo, new: EndfieldOperatorInfo) = old == new
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemEndfieldCharGridBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemEndfieldCharGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.Cultivation.visibility = View.GONE
            binding.ProgressScrim.visibility = View.GONE
        }
        fun bind(info: EndfieldOperatorInfo) {
            val context = binding.root.context
            binding.root.setOnClickListener { onClick(info) }
            binding.Name.text = info.name
            binding.Avatar.load(info.coverUrl) { placeholder(R.drawable.ic_user); error(R.drawable.ic_user) }
            fun icon(view: android.widget.ImageView, id: Int?, key: String) {
                view.visibility = if (id == null) View.GONE else View.VISIBLE
                view.setImageResource(id ?: 0)
                view.contentDescription = EndfieldCharacterVisuals.label(context, key)
            }
            icon(binding.ProfessionIcon, EndfieldCharacterVisuals.professionIcon(info.profession, null), info.profession)
            icon(binding.PropertyIcon, EndfieldCharacterVisuals.propertyIcon(info.property, null), info.property)
            val color = rarityColorMap.getValue(info.rarity)
            val lineColor = when (info.rarity) {
                6 -> R.color.rare_6_line
                5 -> R.color.rare_5_line
                4 -> R.color.rare_4_line
                else -> color
            }
            binding.RarityStripe.setBackgroundColor(ContextCompat.getColor(context, lineColor))
            binding.RarityStripe.contentDescription = "★".repeat(info.rarity)
        }
    }
}
