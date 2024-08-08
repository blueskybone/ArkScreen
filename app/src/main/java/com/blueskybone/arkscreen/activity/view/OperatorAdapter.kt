package com.blueskybone.arkscreen.activity.view

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.base.data.Operator
import com.blueskybone.arkscreen.common.getRarityColorId
import com.blueskybone.arkscreen.util.dpToPx
import org.w3c.dom.Text

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */
class OperatorAdapter(val list: ArrayList<Operator>, private val context: Context) :

    RecyclerView.Adapter<OperatorAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_operator, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        val frameLayout: FrameLayout = itemView.findViewById(R.id.frame_operator)
        val potentialImage: ImageView = itemView.findViewById(R.id.potential_icon)
        val evolveImage: ImageView = itemView.findViewById(R.id.evolve_icon)
        val operatorLevelText: TextView = itemView.findViewById(R.id.envelop_text)
        val nameText: TextView = itemView.findViewById(R.id.text_operator_name)
        val equipsText: TextView = itemView.findViewById(R.id.text_equips)
        val skillLayout: LinearLayout = itemView.findViewById(R.id.skill_layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operator = list[position]
        holder.nameText.text = operator.name
        holder.nameText.setTextColor(
            ContextCompat.getColor(
                context,
                getRarityColorId(operator.rarity + 1)
            )
        )
        holder.operatorLevelText.text = operator.level.toString()
        holder.equipsText.text = operator.equipString
        holder.potentialImage.setImageResource(potentialImageId(operator.potentialRank))
        holder.evolveImage.setImageResource(evolveImageId(operator.evolvePhase))
        holder.skillLayout.removeAllViews()
        for (skill in operator.skills) {
            holder.skillLayout.addView(skillView(skill, operator.mainSkillLvl))
        }
    }

    private fun potentialImageId(num: Int): Int {
        return when (num) {
            0 -> R.drawable.potential_rank_0
            1 -> R.drawable.potential_rank_1
            2 -> R.drawable.potential_rank_2
            3 -> R.drawable.potential_rank_3
            4 -> R.drawable.potential_rank_4
            else -> R.drawable.potential_rank_5
        }
    }

    private fun evolveImageId(num: Int): Int {
        return when (num) {
            2 -> R.drawable.evolve_phase_2
            1 -> R.drawable.evolve_phase_1
            else -> R.drawable.evolve_phase_0
        }
    }

    private fun skillView(skill: Operator.Skill, mainSkillLvl: Int): View {
        val layoutParams = LayoutParams(dpToPx(context, 13F).toInt(), dpToPx(context, 13F).toInt())
        layoutParams.setMargins(dpToPx(context, 5F).toInt())
        when (skill.specializeLevel) {
            1 -> {
                val view = ImageView(context)
                view.setImageResource(R.drawable.special_1)
                view.layoutParams = layoutParams

                return view
            }

            2 -> {
                val view = ImageView(context)
                view.setImageResource(R.drawable.special_2)
                view.layoutParams = layoutParams
                return view

            }

            3 -> {
                val view = ImageView(context)
                view.setImageResource(R.drawable.special_3)
                view.layoutParams = layoutParams

                return view
            }

            else -> {
                val view = TextView(context)
                view.text = mainSkillLvl.toString()
                view.textSize = 10F
                view.layoutParams = layoutParams
                return view
            }
        }
    }
}