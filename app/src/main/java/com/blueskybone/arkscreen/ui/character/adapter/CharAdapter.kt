package com.blueskybone.arkscreen.ui.character.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharBinding
import com.blueskybone.arkscreen.databinding.ItemCharListBinding
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.ui.character.bindAvatarView
import com.blueskybone.arkscreen.ui.character.bindEquipView
import com.blueskybone.arkscreen.ui.character.bindSkillView
import com.blueskybone.arkscreen.ui.character.bindSkillViewLarge
import com.blueskybone.arkscreen.ui.character.evolveIconMap
import com.blueskybone.arkscreen.ui.character.potentialIconMap
import com.blueskybone.arkscreen.ui.character.profIconMap
import com.blueskybone.arkscreen.ui.character.rarityColorMap
import com.blueskybone.arkscreen.ui.character.raritySurfaceColorMap
import com.blueskybone.arkscreen.ui.character.resetEquipView
import com.blueskybone.arkscreen.ui.character.resetSkillView
import com.blueskybone.arkscreen.ui.common.adapter.ItemListener
import com.blueskybone.arkscreen.ui.common.adapter.paging.PagingAdapter


/**
 *   Created by blueskybone
 *   Date: 2025/1/20
 */

class CharAdapter(
    private val context: Context,
    override val PAGE_SIZE: Int,
    private val listener: ItemListener
) : PagingAdapter<Operator, RecyclerView.ViewHolder>() {

    private var currentViewType: ViewType = ViewType.GRID

    companion object {
        private const val VIEW_TYPE_PAYLOAD = "view_type_payload"
    }

    override fun areItemsTheSame(oldItem: Operator, newItem: Operator): Boolean =
        oldItem.charId == newItem.charId

    // 改变视图
    fun setViewType(viewType: ViewType) {
        if (currentViewType != viewType) {
            currentViewType = viewType
            notifyItemRangeChanged(0, itemCount, VIEW_TYPE_PAYLOAD)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentViewType.ordinal
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = context
        val inflater = LayoutInflater.from(context)
        //根据不同的视图选择不同VH
        return when (ViewType.entries[viewType]) {
            ViewType.LIST -> {
                val binding = ItemCharListBinding.inflate(
                    inflater, parent, false
                )
                CharListVH(context, binding, listener)
            }

            ViewType.GRID -> {
                val binding = ItemCharBinding.inflate(
                    inflater, parent, false
                )
                CharGridVH(context, binding, listener)
            }
        }
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: Operator) {
        when (holder) {
            is CharListVH -> holder.bind(item)
            is CharGridVH -> holder.bind(item)
        }
    }

    inner class CharGridVH(
        private val context: Context,
        private val binding: ItemCharBinding,
        listener: ItemListener,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) false else {
                    listener.onLongClick(position)
                    true
                }
            }
            binding.root.setOnClickListener {
                bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?.let(listener::onClick)
            }
        }

        fun bind(item: Operator) {

            binding.Name.text = item.name
            binding.Level.text = item.level.toString()
            val rarity = item.rarity + 1
            binding.Rarity.text = context.getString(R.string.gacha_rarity, rarity)
            binding.Rarity.setTextColor(
                ContextCompat.getColor(
                    context,
                    rarityColorMap[rarity] ?: R.color.rare_1,
                )
            )
            binding.Rarity.backgroundTintList = ContextCompat.getColorStateList(
                context,
                raritySurfaceColorMap[rarity] ?: R.color.rare_1_surface,
            )

            binding.Profession.setImageResource(
                profIconMap[item.profession] ?: R.drawable.skill_icon_default
            )
            binding.Potential.setImageResource(
                potentialIconMap[item.potentialRank] ?: R.drawable.skill_icon_default
            )
            binding.Evolve.setImageResource(
                evolveIconMap[item.evolvePhase] ?: R.drawable.skill_icon_default
            )

            bindAvatarView(binding.Avatar, item.skinId)

            resetSkillView(binding.Skill1)
            resetSkillView(binding.Skill2)
            resetSkillView(binding.Skill3)

            for (skill in item.skills) {
                when (skill.index) {
                    0 -> bindSkillView(context, binding.Skill1, skill, item.mainSkillLvl)
                    1 -> bindSkillView(context, binding.Skill2, skill, item.mainSkillLvl)
                    2 -> bindSkillView(context, binding.Skill3, skill, item.mainSkillLvl)
                    else -> {}
                }
            }
            resetEquipView(binding.Equip1)
            resetEquipView(binding.Equip2)
            resetEquipView(binding.Equip3)

            for (equip in item.equips) {
                when (equip.index) {
                    0 -> bindEquipView(binding.Equip1, equip)
                    1 -> bindEquipView(binding.Equip2, equip)
                    2 -> bindEquipView(binding.Equip3, equip)
                    else -> {}
                }
            }
        }
    }

    inner class CharListVH(
        private val context: Context,
        private val binding: ItemCharListBinding,
        listener: ItemListener,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) false else {
                    listener.onLongClick(position)
                    true
                }
            }
            binding.root.setOnClickListener {
                bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?.let(listener::onClick)
            }
        }

        fun bind(item: Operator) {

            binding.Name.text = item.name
            binding.Level.text = item.level.toString()

            binding.Profession.setImageResource(
                profIconMap[item.profession] ?: R.drawable.skill_icon_default
            )
            binding.Potential.setImageResource(
                potentialIconMap[item.potentialRank] ?: R.drawable.skill_icon_default
            )
            binding.Evolve.setImageResource(
                evolveIconMap[item.evolvePhase] ?: R.drawable.skill_icon_default
            )
            binding.Favor.text = item.favorPercent.toString()

            val colorId = rarityColorMap[item.rarity + 1] ?: R.color.rare_1
            val draw = ContextCompat.getDrawable(context, colorId)
            binding.Avatar.background = draw

            bindAvatarView(binding.Avatar, item.skinId)

            resetSkillView(binding.Skill1)
            resetSkillView(binding.Skill2)
            resetSkillView(binding.Skill3)

            for (skill in item.skills) {
                when (skill.index) {
                    0 -> bindSkillViewLarge(context, binding.Skill1, skill, item.mainSkillLvl)
                    1 -> bindSkillViewLarge(context, binding.Skill2, skill, item.mainSkillLvl)
                    2 -> bindSkillViewLarge(context, binding.Skill3, skill, item.mainSkillLvl)
                    else -> {}
                }
            }
            resetEquipView(binding.Equip1)
            resetEquipView(binding.Equip2)
            resetEquipView(binding.Equip3)

            for (equip in item.equips) {
                when (equip.index) {
                    0 -> bindEquipView(binding.Equip1, equip)
                    1 -> bindEquipView(binding.Equip2, equip)
                    2 -> bindEquipView(binding.Equip3, equip)
                    else -> {}
                }
            }
        }
    }
}
