package com.blueskybone.arkscreen.ui.fragment

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.FlowRadioGroup
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.common.getFlowRadioGroup
import com.blueskybone.arkscreen.common.profImageButton
import com.blueskybone.arkscreen.common.tagButton
import com.blueskybone.arkscreen.databinding.DialogCharBinding
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.databinding.FragmentCharBinding
import com.blueskybone.arkscreen.playerinfo.bindAvatarView
import com.blueskybone.arkscreen.playerinfo.bindEquipView
import com.blueskybone.arkscreen.playerinfo.bindSkillView
import com.blueskybone.arkscreen.playerinfo.evolveIconMap
import com.blueskybone.arkscreen.playerinfo.potentialIconMap
import com.blueskybone.arkscreen.playerinfo.profIconMap
import com.blueskybone.arkscreen.playerinfo.rarityColorMap
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.task.recruit.I18nManager
import com.blueskybone.arkscreen.ui.activity.WebViewActivity
import com.blueskybone.arkscreen.ui.recyclerview.CharAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStrYMD
import com.blueskybone.arkscreen.viewmodel.CharModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import java.net.URLEncoder


/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharOwn : Fragment(), ItemListener {

    private val model: CharModel by activityViewModels()
    private var _binding: FragmentCharBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharAdapter

    //    private lateinit var adapter_new: CharGridAdapter
    private val prefManager: PrefManager by getKoin().inject()
    private var i18nManager: I18nManager = I18nManager.instance

    private val profList =
        listOf("PIONEER", "WARRIOR", "TANK", "SNIPER", "CASTER", "MEDIC", "SUPPORT", "SPECIAL")
    private val profListIcon = listOf(
        R.drawable.icon_pioneer,
        R.drawable.icon_warrior,
        R.drawable.icon_tank,
        R.drawable.icon_sniper,
        R.drawable.icon_caster,
        R.drawable.icon_medic,
        R.drawable.icon_support,
        R.drawable.icon_special
    )
    private val levelList = listOf("精零", "精一", "精二")
    private val rarityList = listOf("1~3★", "4★", "5★", "6★")

    private lateinit var profRadioGroup: FlowRadioGroup
    private lateinit var levelRadioGroup: FlowRadioGroup
    private lateinit var rarityRadioGroup: FlowRadioGroup

    private val adapterListener = object : ItemListener {
        override fun onClick(position: Int) {

            adapter.currentList[position].let { item ->
                val binding = DialogCharBinding.inflate(layoutInflater)
//                binding.Name.text = item.name
                binding.Level.text = item.level.toString()

                val profRsc = profIconMap[item.profession]!!
                ContextCompat.getDrawable(requireContext(), profRsc)

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
                val draw = ContextCompat.getDrawable(requireContext(), colorId)

                binding.Avatar.setBackgroundDrawable(draw)
                bindAvatarView(binding.Avatar, item.skinId)

                binding.Rarity.text = "★".repeat(item.rarity + 1)
                binding.GetTime.text = "获取时间：" + getTimeStrYMD(item.gainTime)
                binding.SubProf.text = "· " + i18nManager.convert(
                    item.subProfessionId,
                    I18nManager.ConvertType.SubProfession
                )

                binding.Love.text = "信赖值：" + item.favorPercent + "%"

                binding.PRTSlink.setOnClickListener {
                    val url = "https://prts.wiki/w/" + URLEncoder.encode(item.name, "UTF-8")
                    if (prefManager.useInnerWeb.get()) {
                        val intent = Intent(requireContext(), WebViewActivity::class.java)
                        intent.putExtra("url", url)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }

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
                        0 -> bindSkillView(
                            requireContext(),
                            binding.Skill1,
                            skill,
                            item.mainSkillLvl
                        )

                        1 -> bindSkillView(
                            requireContext(),
                            binding.Skill2,
                            skill,
                            item.mainSkillLvl
                        )

                        2 -> bindSkillView(
                            requireContext(),
                            binding.Skill3,
                            skill,
                            item.mainSkillLvl
                        )

                        else -> {}
                    }
                }
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

                MaterialAlertDialogBuilder(requireContext())
//                    .setBackground(R.dra)
                    .setView(binding.root)
                    .setTitle(item.name)
                    .show()

            }
        }

        override fun onLongClick(position: Int) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = CharAdapter(requireContext(), 24, adapterListener)
//        adapter_new = CharGridAdapter(requireContext(), 20)
        _binding = FragmentCharBinding.inflate(inflater)

        setupBinding()
        setButtonLayout()
        setupListener()
        return binding.root
    }


    private fun setButtonLayout() {
        val linearLayout = binding.ButtonLayout
        linearLayout.removeAllViews()
        profRadioGroup = getFlowRadioGroup(requireContext())
        for ((idx, prof) in profList.withIndex()) {
            val but = profImageButton(requireContext(), profListIcon[idx], prof)
            profRadioGroup.addView(but)
        }
        linearLayout.addView(profRadioGroup)
        levelRadioGroup = getFlowRadioGroup(requireContext())
        for (level in levelList) {
            val but = tagButton(requireContext(), level)
            levelRadioGroup.addView(but)
        }
        linearLayout.addView(levelRadioGroup)
        rarityRadioGroup = getFlowRadioGroup(requireContext())
        for (rarity in rarityList) {
            val but = tagButton(requireContext(), rarity)
            rarityRadioGroup.addView(but)
        }
        linearLayout.addView(rarityRadioGroup)
    }

    private fun setupListener() {
        val rv = binding.RecyclerView
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    recyclerView.post { adapter.loadMoreData() }
                }
            }
        })
    }

    private fun setupBinding() {

        binding.RecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.RecyclerView.adapter = adapter

//        binding.RecyclerView.adapter = adapter_new
        binding.RecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val params = view.layoutParams as RecyclerView.LayoutParams
                params.width = parent.width / 2
                view.layoutParams = params
            }
        })
        binding.Filter.setOnClickListener {
            if (binding.ButtonLayout.visibility == View.GONE) {
                binding.ButtonLayout.visibility = View.VISIBLE
                binding.FrameDialog.visibility = View.VISIBLE
                binding.ButtonLayout.requestFocus()
            } else {
                binding.ButtonLayout.visibility = View.GONE
                binding.FrameDialog.visibility = View.GONE

                //TODO: model.sublime.filter
                submitFilter()
            }
        }

        binding.ButtonLayout.setOnClickListener {
            // 空实现，拦截点击，防止点击事件穿透
        }
        binding.FrameDialog.setOnClickListener {
            binding.ButtonLayout.visibility = View.GONE
            binding.FrameDialog.visibility = View.GONE

            //TODO: model.sublime.filter
            submitFilter()
        }

        model.charsList.observe(viewLifecycleOwner) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }
    }

    private fun submitFilter() {
        val id1 = profRadioGroup.getCheckedRadioButtonId()
        val filter1 = if (id1 != -1) {
            (profRadioGroup.findViewById<View>(id1) as? ImageButton)?.contentDescription?.toString()
                ?: "ALL"
        } else "ALL"

        val id2 = levelRadioGroup.getCheckedRadioButtonId()
        val filter2 = if (id2 != -1) {
            (levelRadioGroup.findViewById<View>(id2) as? Button)?.text?.toString() ?: "ALL"
        } else "ALL"

        val id3 = rarityRadioGroup.getCheckedRadioButtonId()
        val filter3 = if (id3 != -1) {
            (rarityRadioGroup.findViewById<View>(id3) as? Button)?.text?.toString() ?: "ALL"
        } else "ALL"

        CoroutineScope(Dispatchers.IO).launch {
            model.applyFilter(filter1, filter2, filter3)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onClick(position: Int) {

    }

    override fun onLongClick(position: Int) {

    }
}