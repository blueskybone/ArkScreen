package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharBinding
import com.blueskybone.arkscreen.playerinfo.Operator
import com.blueskybone.arkscreen.playerinfo.bindAvatarView
import com.blueskybone.arkscreen.playerinfo.bindEquipView
import com.blueskybone.arkscreen.playerinfo.bindSkillView
import com.blueskybone.arkscreen.playerinfo.evolveIconMap
import com.blueskybone.arkscreen.playerinfo.potentialIconMap
import com.blueskybone.arkscreen.playerinfo.profIconMap
import com.blueskybone.arkscreen.playerinfo.rarityColorMap
import com.blueskybone.arkscreen.ui.recyclerview.paging.PagingAdapter
import java.net.URLEncoder


/**
 *   Created by blueskybone
 *   Date: 2025/1/20
 */

class CharAdapter(
    private val context: Context,
    override val PAGE_SIZE: Int,
    private val listener: ItemListener
) :
    PagingAdapter<Operator, CharAdapter.OperatorVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCharBinding.inflate(inflater, parent, false)
        return OperatorVH(context, binding, listener)
    }

    override fun onBindViewHolder(holder: OperatorVH, position: Int) {
        holder.bind(currentList[position])
    }


    inner class OperatorVH(
        private val context: Context,
        private val binding: ItemCharBinding,
        listener: ItemListener,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                listener.onLongClick(bindingAdapterPosition)
                true
            }
            binding.root.setOnClickListener {
                listener.onClick(bindingAdapterPosition)
            }
        }

        fun bind(item: Operator) {

            binding.Name.text = item.name
            binding.Level.text = item.level.toString()

            val profRsc = profIconMap[item.profession]!!
            ContextCompat.getDrawable(context, profRsc)

            binding.Profession.setImageResource(
                profIconMap[item.profession] ?: R.drawable.skill_icon_default
            )
            binding.Potential.setImageResource(
                potentialIconMap[item.potentialRank] ?: R.drawable.skill_icon_default
            )
            binding.Evolve.setImageResource(
                evolveIconMap[item.evolvePhase] ?: R.drawable.skill_icon_default
            )

            val colorId = rarityColorMap[item.rarity + 1] ?: R.color.red
            val draw = ContextCompat.getDrawable(context, colorId)

            bindAvatarView(binding.Avatar, item.skinId)

            binding.Skill1.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Skill2.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Skill3.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Skill1.Icon.alpha = 0.0F
            binding.Skill2.Icon.alpha = 0.0F
            binding.Skill3.Icon.alpha = 0.0F
            binding.Skill1.Special.visibility = View.GONE
            binding.Skill2.Special.visibility = View.GONE
            binding.Skill3.Special.visibility = View.GONE
            binding.Skill1.MainRank.visibility = View.GONE
            binding.Skill2.MainRank.visibility = View.GONE
            binding.Skill3.MainRank.visibility = View.GONE

            for (skill in item.skills) {
                when (skill.index) {
                    0 -> bindSkillView(context, binding.Skill1, skill, item.mainSkillLvl)
                    1 -> bindSkillView(context, binding.Skill2, skill, item.mainSkillLvl)
                    2 -> bindSkillView(context, binding.Skill3, skill, item.mainSkillLvl)
                    else -> {}
                }
            }
            binding.Equip1.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Equip2.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Equip3.Icon.setImageResource(R.drawable.skill_icon_default)
            binding.Equip1.Icon.alpha = 0.0F
            binding.Equip2.Icon.alpha = 0.0F
            binding.Equip3.Icon.alpha = 0.0F
            binding.Equip1.Stage.visibility = View.GONE
            binding.Equip2.Stage.visibility = View.GONE
            binding.Equip3.Stage.visibility = View.GONE

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