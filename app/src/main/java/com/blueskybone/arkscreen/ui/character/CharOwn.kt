package com.blueskybone.arkscreen.ui.character

import android.os.Bundle
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ConcatAdapter
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.DialogCharBinding
import com.blueskybone.arkscreen.databinding.FragmentCharBinding
import com.blueskybone.arkscreen.domain.service.TextTranslator
import com.blueskybone.arkscreen.ui.character.adapter.CharAdapter
import com.blueskybone.arkscreen.ui.character.adapter.CharHeaderAdapter
import com.blueskybone.arkscreen.ui.character.adapter.ViewType
import com.blueskybone.arkscreen.ui.common.adapter.ItemListener
import com.blueskybone.arkscreen.ui.common.view.FlowRadioGroup
import com.blueskybone.arkscreen.ui.common.view.getFlowRadioGroup
import com.blueskybone.arkscreen.ui.common.view.profImageButton
import com.blueskybone.arkscreen.ui.common.view.tagButton
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStrYMD
import com.blueskybone.arkscreen.ui.common.openLink
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.net.URLEncoder


/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharOwn : Fragment() {

    private val model: CharModel by activityViewModel()
    private var _binding: FragmentCharBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharAdapter
    private val headerAdapter = CharHeaderAdapter()
    private val launcherForTxt =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            val context = context ?: return@registerForActivityResult
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            output.write(model.generateExportText().toByteArray())
                        } ?: error("无法创建导出文件")
                    }
                }
                result.onSuccess { Toaster.show("导出完成") }
                    .onFailure { Toaster.show("导出失败：${it.message}") }
            }
        }

    private val prefManager: SettingPrefManager by getKoin().inject()
    private val textTranslator: TextTranslator by getKoin().inject()

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
    private var currentViewType: ViewType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharBinding.inflate(inflater)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CharAdapter(requireContext(), 24, adapterListener)
        setupBinding()
        setUpObserver()
        setButtonLayout()
        setupListener()
    }

    private fun updateLayout(viewType: ViewType) {
        if (currentViewType == viewType) return
        currentViewType = viewType
        adapter.setViewType(viewType)

        when (viewType) {
            ViewType.LIST -> {
                binding.RecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.ViewChanger.setIconResource(R.drawable.ic_list)
            }

            ViewType.GRID -> {
                binding.RecyclerView.layoutManager = createGridLayoutManager()
                binding.ViewChanger.setIconResource(R.drawable.ic_grid)
            }
        }
        headerAdapter.setViewType(viewType)
        binding.RecyclerView.invalidateItemDecorations()
    }

    private fun setButtonLayout() {
        val linearLayout = binding.FilterContent
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
                if (dy > 0 && adapter.hasMore && !recyclerView.canScrollVertically(1)) {
                    recyclerView.post { adapter.loadMoreData() }
                }
            }
        })
    }

    private fun setupBinding() {

        binding.RecyclerView.layoutManager = createGridLayoutManager()
        binding.RecyclerView.adapter = ConcatAdapter(headerAdapter, adapter)
        binding.RecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val outer = (11 * resources.displayMetrics.density).toInt()
            private val inner = (2 * resources.displayMetrics.density).toInt()

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                if ((currentViewType ?: ViewType.GRID) != ViewType.GRID) return
                val position = parent.getChildAdapterPosition(view)
                if (position <= 0) return
                val gridPosition = position - 1
                if (gridPosition % 2 == 0) {
                    outRect.set(outer, 0, inner, 0)
                } else {
                    outRect.set(inner, 0, outer, 0)
                }
            }
        })

        binding.Filter.setOnClickListener {
            if (binding.ButtonLayout.isGone) {
                binding.ButtonLayout.visibility = View.VISIBLE
                binding.FrameDialog.visibility = View.VISIBLE
                binding.ButtonLayout.requestFocus()
            } else {
                binding.ButtonLayout.visibility = View.GONE
                binding.FrameDialog.visibility = View.GONE

                submitFilter()
            }
        }

        binding.ButtonLayout.setOnClickListener {
            // 空实现，拦截点击，防止点击事件穿透
        }


        binding.Export.setOnClickListener {
            val accountName = model.exportFileBaseName()
            launcherForTxt.launch("${accountName}_char_assets")

        }
        binding.FrameDialog.setOnClickListener {
            binding.ButtonLayout.visibility = View.GONE
            binding.FrameDialog.visibility = View.GONE

            submitFilter()
        }
        binding.ViewChanger.setOnClickListener {
            model.toggleViewType()
        }
    }

    private fun createGridLayoutManager() =
        GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    if (position == 0) spanCount else 1
            }
        }

    private fun setUpObserver() {
        model.charsList.observe(viewLifecycleOwner) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }
        model.statistic.observe(viewLifecycleOwner) { statistic ->
            headerAdapter.submitStatistic(statistic)
        }
        model.currentAccount.observe(viewLifecycleOwner) { account ->
            headerAdapter.submitAccount(account)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.viewType.collect { viewType ->
                    updateLayout(viewType)
                }
            }
        }
    }

    private fun submitFilter() {
        val id1 = profRadioGroup.getCheckedRadioButtonId()
        val filter1 = id1.takeIf { it != View.NO_ID }
            ?.let { profRadioGroup.findViewById<View>(it) }
            ?.let { profRadioGroup.indexOfChild(it) }
            ?.let(profList::getOrNull)

        val id2 = levelRadioGroup.getCheckedRadioButtonId()
        val filter2 = id2.takeIf { it != View.NO_ID }
            ?.let { levelRadioGroup.findViewById<View>(it) }
            ?.let { levelRadioGroup.indexOfChild(it) }
            ?.let { CharModel.EvolveFilter.entries.getOrNull(it + 1) }
            ?: CharModel.EvolveFilter.ALL

        val id3 = rarityRadioGroup.getCheckedRadioButtonId()
        val filter3 = id3.takeIf { it != View.NO_ID }
            ?.let { rarityRadioGroup.findViewById<View>(it) }
            ?.let { rarityRadioGroup.indexOfChild(it) }
            ?.let { CharModel.RarityFilter.entries.getOrNull(it + 1) }
            ?: CharModel.RarityFilter.ALL

        model.applyFilter(filter1, filter2, filter3)
    }

    private val adapterListener = object : ItemListener {
        override fun onClick(position: Int) {

            adapter.currentList.getOrNull(position)?.let { item ->
                val binding = DialogCharBinding.inflate(layoutInflater)
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

                val colorId = rarityColorMap[item.rarity + 1] ?: R.color.red
                val draw = ContextCompat.getDrawable(requireContext(), colorId)

                binding.Avatar.background = draw
                bindAvatarView(binding.Avatar, item.skinId)

                binding.Rarity.text = "★".repeat(item.rarity + 1)
                binding.GetTime.text = "获取时间：" + getTimeStrYMD(item.gainTime)
                binding.SubProf.text = "· ${item.subProfessionId}"
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.SubProf.text = "· " + textTranslator.translate(
                        item.subProfessionId,
                        item.subProfessionId
                    )
                }

                binding.Love.text = "信赖值：" + item.favorPercent + "%"

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
                    .setView(binding.root)
                    .setPositiveButton(R.string.goto_PRTS) { _, _ ->
                        val url =
                            "https://prts.wiki/w/" + URLEncoder.encode(item.name, "UTF-8")
                        openLink(requireContext(), url, prefManager)
                    }
                    .show()

            }
        }

        override fun onLongClick(position: Int) {
        }
    }

    override fun onDestroyView() {
        binding.RecyclerView.adapter = null
        _binding = null
        currentViewType = null
        super.onDestroyView()
    }
}
