package com.blueskybone.arkscreen.ui.character

import android.os.Bundle
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.model.EndfieldCharacter
import com.blueskybone.arkscreen.data.network.model.EndfieldWeapon
import com.blueskybone.arkscreen.data.network.model.EndfieldTalent
import com.blueskybone.arkscreen.databinding.FragmentEndfieldCharDetailBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharDetailSectionBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharEquipmentBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharTacticalBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharDetailWeaponBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharSkillBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharTalentGroupBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharTalentIconBinding
import com.blueskybone.arkscreen.platform.time.TimeUtils.getTimeStr
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EndfieldCharDetailFragment : Fragment() {
    companion object {
        private const val ARG_CHARACTER_ID = "character_id"

        fun newInstance(characterId: String) = EndfieldCharDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_CHARACTER_ID, characterId) }
        }
    }

    private val model: EndfieldCharModel by activityViewModel()
    private var _binding: FragmentEndfieldCharDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEndfieldCharDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getString(ARG_CHARACTER_ID).orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    val character = state.characters.firstOrNull {
                        it.id == id || it.charData?.id == id
                    }
                    if (character == null) {
                        binding.HeroCard.visibility = View.GONE
                        binding.Sections.removeAllViews()
                        binding.Status.visibility = View.VISIBLE
                        binding.Status.text = if (state.loading) {
                            getString(R.string.endfield_chars_loading)
                        } else {
                            getString(R.string.endfield_char_unavailable)
                        }
                    } else {
                        binding.Status.visibility = View.GONE
                        render(character)
                    }
                }
            }
        }
    }

    private fun render(character: EndfieldCharacter) {
        val data = character.charData
        binding.HeroCard.visibility = View.VISIBLE
        binding.Name.text = data?.name?.takeIf(String::isNotBlank)
            ?: character.id.orEmpty()
        binding.Rarity.text = EndfieldCharDisplay.stars(data?.rarity?.value, data?.rarity?.key)
        binding.Rarity.visibility = if (binding.Rarity.text.isEmpty()) View.GONE else View.VISIBLE
        val professionIcon = EndfieldCharacterVisuals.professionIcon(
            data?.profession?.key, data?.profession?.value,
        )
        binding.ProfessionIcon.visibility = if (professionIcon == null) View.GONE else View.VISIBLE
        professionIcon?.let(binding.ProfessionIcon::setImageResource)
        binding.ProfessionIcon.contentDescription = data?.profession?.value
        val propertyIcon = EndfieldCharacterVisuals.propertyIcon(
            data?.property?.key, data?.property?.value,
        )
        binding.PropertyIcon.visibility = if (propertyIcon == null) View.GONE else View.VISIBLE
        propertyIcon?.let(binding.PropertyIcon::setImageResource)
        binding.PropertyIcon.contentDescription = data?.property?.value
        binding.Tags.removeAllViews()
        data?.tags.orEmpty().filter(String::isNotBlank).forEach { tag ->
            binding.Tags.addView(createTag(tag))
        }
        binding.Progress.text = listOfNotNull(
            character.level?.let { getString(R.string.endfield_char_level, it) },
            character.evolvePhase?.let { getString(R.string.endfield_char_evolve, it) },
            character.potentialLevel?.let { getString(R.string.endfield_char_potential, it) },
        ).joinToString(" · ")
        val obtainedAt = character.ownTs?.toLongOrNull()?.takeIf { it > 0L }
        binding.Obtained.visibility = if (obtainedAt == null) View.GONE else View.VISIBLE
        if (obtainedAt != null) {
            binding.Obtained.text = getString(
                R.string.endfield_char_obtained,
                getTimeStr(obtainedAt * 1000L, "yyyy-MM-dd"),
            )
        }
        binding.Avatar.load(
            data?.avatarRtUrl?.takeIf(String::isNotBlank)
                ?: data?.avatarSqUrl?.takeIf(String::isNotBlank)
        ) {
            placeholder(R.drawable.ic_user)
            error(R.drawable.ic_user)
        }

        binding.Sections.removeAllViews()
        character.weapon?.let(::addWeaponSection)
        addSkillsSection(character)
        addEquipmentSection(character)
        addTalentsSection(character)
    }

    private fun addSkillsSection(character: EndfieldCharacter) {
        val skills = character.charData?.skills.orEmpty()
            .sortedBy { EndfieldCharacterVisuals.skillOrder(it.type?.key) }
        if (skills.isEmpty()) return
        val section = ItemEndfieldCharDetailSectionBinding.inflate(layoutInflater, binding.Sections, false)
        section.Title.setText(R.string.endfield_char_skills)
        val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        skills.forEach { skill ->
            val item = ItemEndfieldCharSkillBinding.inflate(layoutInflater, row, false)
            item.root.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f,
            )
            val skillProperty = skill.property?.key ?: character.charData?.property?.key
            val accent = ContextCompat.getColor(requireContext(),
                EndfieldCharacterVisuals.propertyColor(skillProperty))
            item.IconFrame.background = EndfieldIconBackground(
                accent, EndfieldCharacterVisuals.isUltimate(skill.type?.key),
            )
            item.Icon.imageTintList = ColorStateList.valueOf(Color.WHITE)
            skill.iconUrl?.takeIf(String::isNotBlank)?.let(item.Icon::load)
            val userSkill = character.userSkills?.get(skill.id)
                ?: character.userSkills?.values?.firstOrNull { it.skillId == skill.id }
            val level = userSkill?.level
            item.Rank.text = when {
                level == null -> "–"
                level >= 10 -> "专精 ${level - 9}"
                else -> "RANK $level"
            }
            item.Name.text = skill.name?.takeIf(String::isNotBlank)
                ?: getString(R.string.endfield_char_unknown_skill)
            row.addView(item.root)
        }
        section.Rows.addView(row)
        binding.Sections.addView(section.root)
    }

    private fun addTalentsSection(character: EndfieldCharacter) {
        val data = character.charData ?: return
        val categories = listOf(
            Triple(R.string.endfield_char_ability_talent, data.abilityTalents.orEmpty(), "ability"),
            Triple(R.string.endfield_char_combat_talent, data.combatTalents.orEmpty(), "combat"),
            Triple(R.string.endfield_char_cultivation_talent, data.cultivationTalents.orEmpty(), "cultivation"),
        )
        if (categories.all { it.second.isEmpty() }) return
        val section = ItemEndfieldCharDetailSectionBinding.inflate(layoutInflater, binding.Sections, false)
        section.Title.setText(R.string.endfield_char_talents)
        categories.forEach { (titleRes, talents, category) ->
            if (talents.isEmpty()) return@forEach
            talents.groupBy { EndfieldCharacterVisuals.talentGroupKey(it.id, it.name) }
                .values.forEach { group ->
                    val row = ItemEndfieldCharTalentGroupBinding.inflate(
                        layoutInflater, section.Rows, false,
                    )
                    row.Name.text = group.first().name?.substringBefore('·')
                        ?.takeIf(String::isNotBlank) ?: getString(titleRes)
                    group.sortedBy { EndfieldCharacterVisuals.talentTier(it.id) }
                        .forEach { talent -> addTalentIcon(row, talent, character, category) }
                    section.Rows.addView(row.root)
                }
        }
        binding.Sections.addView(section.root)
    }

    private fun addTalentIcon(
        row: ItemEndfieldCharTalentGroupBinding,
        talent: EndfieldTalent,
        character: EndfieldCharacter,
        category: String,
    ) {
        val icon = ItemEndfieldCharTalentIconBinding.inflate(layoutInflater, row.Icons, false)
        val cultivation = category == "cultivation"
        val unlocked = if (cultivation) null else EndfieldCharacterVisuals.isTalentUnlocked(
            talent.id, character.talent, category,
        )
        if (cultivation) {
            icon.Icon.setPadding(0, 0, 0, 0)
        } else {
            icon.IconFrame.background = EndfieldIconBackground(
                ContextCompat.getColor(requireContext(),
                    if (unlocked == true) R.color.ef_talent_active else R.color.ef_talent_locked),
                true,
            )
            icon.Icon.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
        val url = if (cultivation || unlocked == true) talent.iconUrl
            else talent.lockedIconUrl?.takeIf(String::isNotBlank) ?: talent.iconUrl
        url?.takeIf(String::isNotBlank)?.let(icon.Icon::load)
        icon.root.contentDescription = listOfNotNull(
            talent.name,
            if (cultivation) null else if (unlocked == true) "已解锁" else "未解锁",
        ).joinToString("，")
        row.Icons.addView(icon.root)
    }

    private fun addWeaponSection(weapon: EndfieldWeapon) {
        val info = weapon.weaponData
        val section = ItemEndfieldCharDetailWeaponBinding.inflate(
            layoutInflater, binding.Sections, false,
        )
        section.WeaponName.text = info?.name?.takeIf(String::isNotBlank)
            ?: getString(R.string.endfield_char_unknown_weapon)
        section.WeaponRarity.text = EndfieldCharDisplay.stars(
            info?.rarity?.value, info?.rarity?.key,
        )
        section.WeaponRarity.visibility =
            if (section.WeaponRarity.text.isEmpty()) View.GONE else View.VISIBLE
        section.WeaponProgress.text = listOfNotNull(
            weapon.level?.let { getString(R.string.endfield_char_level, it) },
            weapon.refineLevel?.let { getString(R.string.endfield_char_refine, it) },
            weapon.breakthroughLevel?.let { getString(R.string.endfield_char_breakthrough, it) },
        ).joinToString(" · ")
        info?.iconUrl?.takeIf(String::isNotBlank)?.let { url ->
            section.WeaponIconFrame.visibility = View.VISIBLE
            section.WeaponIcon.load(url)
        }

        weapon.gem?.let { gem ->
            section.GemContainer.visibility = View.VISIBLE
            section.GemName.text = gem.gemData?.name?.takeIf(String::isNotBlank)
                ?: getString(R.string.endfield_char_gem)
            section.GemQuality.text = EndfieldCharDisplay.gemQuality(gem.gemData?.templateId)
            val gemIcon = gem.gemData?.icon?.takeIf { it.startsWith("http") }
                ?: gem.icon?.takeIf { it.startsWith("http") }
            gemIcon?.let { url ->
                section.GemIconFrame.visibility = View.VISIBLE
                section.GemIcon.load(url)
            }
            gem.terms.orEmpty().forEach { term ->
                val name = term.name?.takeIf(String::isNotBlank) ?: return@forEach
                val text = if (term.cost == null) name else "$name · ${term.cost}"
                section.GemTerms.addView(createTag(text))
            }
        }
        binding.Sections.addView(section.root)
    }

    private fun createTag(label: String): TextView = TextView(requireContext()).apply {
        text = label
        setBackgroundResource(R.drawable.bg_asset_stat_chip)
        setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Caption)
        setTextColor(ContextCompat.getColor(requireContext(), R.color.on_sec_con))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(dp(9), dp(4), dp(9), dp(4))
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).toInt()

    private fun qualityFrame(rarityKey: String?): GradientDrawable {
        val color = ContextCompat.getColor(requireContext(),
            EndfieldEquipmentDisplay.qualityColor(rarityKey))
        return GradientDrawable().apply {
            cornerRadius = dp(2).toFloat()
            setColor(ContextCompat.getColor(requireContext(), R.color.ef_item_icon_background))
            setStroke(dp(1).coerceAtLeast(1), color)
        }
    }

    private fun addEquipmentSection(character: EndfieldCharacter) {
        val equipment = listOf(
            character.bodyEquip,
            character.armEquip,
            character.firstAccessory,
            character.secondAccessory,
        )
        val section = ItemEndfieldCharDetailSectionBinding.inflate(
            layoutInflater, binding.Sections, false,
        )
        section.Title.setText(R.string.endfield_char_equipment)
        equipment.chunked(2).forEach { pair ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            pair.forEach { equipped ->
                val item = ItemEndfieldCharEquipmentBinding.inflate(layoutInflater, row, false)
                item.root.layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f,
                )
                val data = equipped?.equipData
                item.Name.text = data?.name?.takeIf(String::isNotBlank)
                    ?: getString(R.string.endfield_char_unequipped)
                item.Suit.text = data?.suit?.name?.takeIf(String::isNotBlank).orEmpty()
                item.Suit.visibility = if (item.Suit.text.isEmpty()) View.GONE else View.VISIBLE
                item.Level.text = data?.level?.value?.takeIf(String::isNotBlank)?.let {
                    getString(R.string.endfield_char_equipment_level, it)
                }.orEmpty()
                item.Level.visibility = if (item.Level.text.isEmpty()) View.GONE else View.VISIBLE
                item.Refine.text = if (data == null) "" else getString(
                    R.string.endfield_char_equipment_refine_progress,
                    EndfieldEquipmentDisplay.refineProgress(equipped.enhance),
                )
                item.Refine.visibility = if (data == null) View.GONE else View.VISIBLE
                item.IconFrame.visibility = if (data == null) View.INVISIBLE else View.VISIBLE
                if (data != null) {
                    item.IconFrame.background = qualityFrame(data.rarity?.key)
                    data.iconUrl?.takeIf(String::isNotBlank)?.let(item.Icon::load)
                }
                row.addView(item.root)
            }
            section.Rows.addView(row)
        }
        character.tacticalItem?.tacticalItemData?.let { data ->
            val tactical = ItemEndfieldCharTacticalBinding.inflate(
                layoutInflater, section.Rows, false,
            )
            tactical.Name.text = data.name?.takeIf(String::isNotBlank)
                ?: getString(R.string.endfield_char_unknown_item)
            tactical.IconFrame.background = qualityFrame(data.rarity?.key)
            data.iconUrl?.takeIf(String::isNotBlank)?.let(tactical.Icon::load)
            section.Rows.addView(tactical.root)
        }
        binding.Sections.addView(section.root)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
