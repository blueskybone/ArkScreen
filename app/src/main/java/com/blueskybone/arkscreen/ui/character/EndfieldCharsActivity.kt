package com.blueskybone.arkscreen.ui.character

import android.content.Intent
import androidx.appcompat.widget.ListPopupWindow
import com.blueskybone.arkscreen.ui.account.common.showAccountChoices
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.model.EndfieldCharacter
import com.blueskybone.arkscreen.databinding.ActivityEndfieldCharsBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldCharBinding
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndfieldCharsActivity : AppCompatActivity() {
    private val model: EndfieldCharModel by viewModel()
    private lateinit var binding: ActivityEndfieldCharsBinding
    private val adapter = CharacterAdapter(::openDetail)
    private var currentState = EndfieldCharUiState()

    private val settings: com.blueskybone.arkscreen.data.local.pref.SettingPrefManager by inject()
    private var grid = false
    private var rarityFilter: String? = null
    private var professionFilter: String? = null
    private var propertyFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldCharsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rarityFilter = savedInstanceState?.getString("rarityFilter")
        professionFilter = savedInstanceState?.getString("professionFilter")
        propertyFilter = savedInstanceState?.getString("propertyFilter")
        grid = savedInstanceState?.getBoolean("grid") ?: settings.endfieldAssetsGrid.get()
        binding.Characters.adapter = adapter
        fun applyView() {
            adapter.grid = grid
            binding.Characters.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, if (grid) 4 else 1)
            // Item margins plus RecyclerView padding match the list card's outer inset.
            val sidePadding = if (grid) {
                (resources.getDimensionPixelSize(R.dimen.page_horizontal_spacing) -
                    resources.getDimensionPixelSize(R.dimen.endfield_grid_item_margin)).coerceAtLeast(0)
            } else 0
            binding.Characters.setPaddingRelative(sidePadding, binding.Characters.paddingTop,
                sidePadding, binding.Characters.paddingBottom)
            binding.ViewMode.setChipIconResource(if (grid) R.drawable.ic_grid else R.drawable.ic_list)
            adapter.notifyDataSetChanged()
        }
        binding.ViewMode.setOnClickListener { grid = !grid; settings.endfieldAssetsGrid.set(grid); applyView() }
        applyView()
        binding.Filter.setOnClickListener { showFilterDialog() }
        supportFragmentManager.addOnBackStackChangedListener { updatePage(animate = true) }
        updatePage()
        binding.AccountRow.AccountSelector.root.setOnClickListener { chooseAccount() }
        binding.RefreshButton.setOnClickListener { model.refresh() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect(::render)
            }
        }
    }

    private fun render(state: EndfieldCharUiState) {
        currentState = state
        binding.ProfileCard.visibility = if (state.loading) View.GONE else View.VISIBLE
        binding.Characters.visibility = if (state.loading) View.GONE else View.VISIBLE
        binding.Loading.setMessage(R.string.endfield_chars_loading)
        val selected = state.accounts.firstOrNull { it.uid == state.selectedUid }
        binding.AccountRow.AccountSelector.CurrentAccount.text = selected?.nickName ?: getString(R.string.endfield_choose_account)
        binding.AccountRow.AccountSelector.Server.setText(if (selected?.official == true) R.string.official_server else R.string.bilibili_server)
        binding.AccountRow.AccountSelector.Server.visibility = View.GONE
        binding.AccountRow.Avatar.load(state.avatarUrl) { placeholder(R.drawable.ic_user); error(R.drawable.ic_user) }
        binding.AccountRow.Avatar.setOnClickListener { chooseAccount() }
        binding.Loading.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.RefreshButton.isEnabled = selected != null && !state.loading
        val filtered = state.characters.filter { character ->
            (rarityFilter == null || filterValue(character, 0) == rarityFilter) &&
                (professionFilter == null || filterValue(character, 1) == professionFilter) &&
                (propertyFilter == null || filterValue(character, 2) == propertyFilter)
        }
        adapter.submitList(filtered.sortedByDescending { it.level ?: -1 })
        binding.Filters.visibility = if (state.characters.isNotEmpty() && !state.loading) View.VISIBLE else View.GONE
        val active = rarityFilter != null || professionFilter != null || propertyFilter != null
        binding.Filter.text = if (active) getString(R.string.assets_filter_count, filtered.size) else getString(R.string.filter)
        val color = getColor(if (active) R.color.asset_action_selected_text else R.color.home_text_primary)
        binding.Filter.setTextColor(color)
        binding.Filter.chipIconTint = android.content.res.ColorStateList.valueOf(color)
        val message = when {
            state.loading -> null
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.error != null -> state.error
            state.characters.isEmpty() -> getString(R.string.endfield_chars_empty)
            filtered.isEmpty() -> "没有符合筛选条件的干员"
            else -> null
        }
        binding.EmptyMessage.text = message
        binding.EmptyMessage.visibility = if (message == null) View.GONE else View.VISIBLE
    }

    private fun filterValue(character: EndfieldCharacter, kind: Int): String? = when (kind) {
        0 -> EndfieldCharDisplay.stars(character.charData?.rarity?.value, character.charData?.rarity?.key)
        1 -> character.charData?.profession?.value
        else -> character.charData?.property?.value
    }?.takeIf(String::isNotBlank)

    private fun showFilterDialog() {
        val content = com.blueskybone.arkscreen.databinding.DialogEndfieldCharFilterBinding.inflate(layoutInflater)
        val selected = listOf(rarityFilter, professionFilter, propertyFilter)
        val options = (0..2).map { kind -> currentState.characters.mapNotNull { filterValue(it, kind) }.distinct().sortedDescending() }
        val groups = listOf(content.Rarity, content.Profession, content.Property).mapIndexed { index, container ->
            com.blueskybone.arkscreen.ui.common.view.getFlowRadioGroup(this).also { group ->
                options[index].forEach { value ->
                    val button = com.blueskybone.arkscreen.ui.common.view.tagButton(this, value)
                    button.id = View.generateViewId()
                    group.addView(button)
                    if (value == selected[index]) group.check(button.id)
                }
                container.addView(group)
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.filter)
            .setView(content.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val values = groups.mapIndexed { index, group ->
                    val checked = group.findViewById<View>(group.getCheckedRadioButtonId())
                    options[index].getOrNull(if (checked == null) -1 else group.indexOfChild(checked))
                }
                rarityFilter = values[0]; professionFilter = values[1]; propertyFilter = values[2]
                render(currentState)
                binding.Characters.scrollToPosition(0)
            }
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.clear) { _, _ ->
                rarityFilter = null; professionFilter = null; propertyFilter = null
                render(currentState)
                binding.Characters.scrollToPosition(0)
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("grid", grid)
        outState.putString("rarityFilter", rarityFilter)
        outState.putString("professionFilter", professionFilter)
        outState.putString("propertyFilter", propertyFilter)
        super.onSaveInstanceState(outState)
    }

    private fun chooseAccount() {
        val accounts = currentState.accounts
        if (accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        accountPopup?.dismiss()
        accountPopup = showAccountChoices(binding.AccountRow.AccountSelector.root, accounts, currentState.selectedUid) { account ->
            model.select(account.uid)
        }
    }

    private fun openDetail(character: EndfieldCharacter) {
        val id = character.id ?: character.charData?.id ?: return
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.page_fade_in, R.anim.page_fade_out, R.anim.page_fade_in, R.anim.page_fade_out)
            replace(R.id.DetailContainer, EndfieldCharDetailFragment.newInstance(id))
            addToBackStack("endfield_character")
        }
    }

    private fun updatePage(animate: Boolean = false) {
        val showingDetail = supportFragmentManager.backStackEntryCount > 0
        binding.RefreshButton.visibility = if (showingDetail) View.GONE else View.VISIBLE
        binding.ListContent.animate().cancel()
        binding.DetailContainer.removeCallbacks(hideDetail)
        if (animate) {
            binding.ListContent.visibility = View.VISIBLE
            binding.DetailContainer.visibility = View.VISIBLE
            binding.ListContent.animate().alpha(if (showingDetail) 0f else 1f).setDuration(180L)
                .withEndAction {
                    if (supportFragmentManager.backStackEntryCount > 0) binding.ListContent.visibility = View.GONE
                }.start()
            // Keep the container visible until the fragment's pop-exit fade finishes.
            if (!showingDetail) binding.DetailContainer.postDelayed(hideDetail, 180L)
        } else {
            binding.ListContent.alpha = if (showingDetail) 0f else 1f
            binding.ListContent.visibility = if (showingDetail) View.GONE else View.VISIBLE
            binding.DetailContainer.visibility = if (showingDetail) View.VISIBLE else View.GONE
        }
        binding.Toolbar.setTitle(
            if (showingDetail) R.string.endfield_char_detail else R.string.operator_assets
        )
    }

    private val hideDetail = Runnable {
        if (supportFragmentManager.backStackEntryCount == 0) binding.DetailContainer.visibility = View.GONE
    }

    private var accountPopup: ListPopupWindow? = null

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
        return true
    }

    override fun onDestroy() {
        binding.ListContent.animate().cancel()
        binding.DetailContainer.removeCallbacks(hideDetail)
        binding.Characters.adapter = null
        accountPopup?.dismiss()
        super.onDestroy()
    }

    private class CharacterAdapter(
        private val onClick: (EndfieldCharacter) -> Unit,
    ) : ListAdapter<EndfieldCharacter, CharacterAdapter.Holder>(
        object : DiffUtil.ItemCallback<EndfieldCharacter>() {
            override fun areItemsTheSame(old: EndfieldCharacter, new: EndfieldCharacter) =
                old.id == new.id

            override fun areContentsTheSame(old: EndfieldCharacter, new: EndfieldCharacter) =
                old == new
        }
    ) {
        var grid = false
        override fun getItemViewType(position: Int) = if (grid) 1 else 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
            LayoutInflater.from(parent.context).inflate(
                if (viewType == 1) R.layout.item_endfield_char_grid else R.layout.item_endfield_char,
                parent, false
            )
        )
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position), onClick)
        }
        class Holder(view: View) : RecyclerView.ViewHolder(view) {
            private val gridBinding = if (view.findViewById<View>(R.id.Potential) != null)
                com.blueskybone.arkscreen.databinding.ItemEndfieldCharGridBinding.bind(view) else null
            private val listBinding = if (gridBinding == null) ItemEndfieldCharBinding.bind(view) else null
            private fun bindGrid(
                binding: com.blueskybone.arkscreen.databinding.ItemEndfieldCharGridBinding,
                character: EndfieldCharacter,
                onClick: (EndfieldCharacter) -> Unit,
            ) {
                val data = character.charData
                binding.root.setOnClickListener { onClick(character) }
                binding.Name.text = data?.name?.takeIf(String::isNotBlank) ?: character.id.orEmpty()
                binding.Avatar.load(data?.avatarRtUrl?.takeIf(String::isNotBlank)
                    ?: data?.avatarSqUrl?.takeIf(String::isNotBlank)) {
                    placeholder(R.drawable.ic_user)
                    error(R.drawable.ic_user)
                }
                fun icon(view: android.widget.ImageView, resource: Int?, label: String?) {
                    view.visibility = if (resource == null) View.GONE else View.VISIBLE
                    view.setImageResource(resource ?: 0)
                    view.contentDescription = label
                }
                icon(binding.ProfessionIcon, EndfieldCharacterVisuals.professionIcon(data?.profession?.key, data?.profession?.value), data?.profession?.value)
                icon(binding.PropertyIcon, EndfieldCharacterVisuals.propertyIcon(data?.property?.key, data?.property?.value), data?.property?.value)
                binding.Level.text = character.level?.let { "Lv.$it" }.orEmpty()
                binding.Evolve.text = character.evolvePhase?.let { "精$it" }.orEmpty()
                binding.Evolve.contentDescription = character.evolvePhase?.let { binding.root.context.getString(R.string.endfield_char_evolve, it) }
                binding.Potential.text = character.potentialLevel?.let { "潜$it" }.orEmpty()
                binding.Potential.contentDescription = character.potentialLevel?.let { "潜能 $it" }
                bindRarity(binding.RarityStripe, character)
            }

            private fun bindRarity(stripe: View, character: EndfieldCharacter) {
                val data = character.charData
                val stars = EndfieldCharDisplay.stars(data?.rarity?.value, data?.rarity?.key)
                val color = when (stars.length) {
                    6 -> R.color.rare_6_line
                    5 -> R.color.rare_5_line
                    4 -> R.color.rare_4_line
                    else -> R.color.grey_500
                }
                stripe.setBackgroundColor(androidx.core.content.ContextCompat.getColor(stripe.context, color))
                stripe.contentDescription = stars
            }

            fun bind(character: EndfieldCharacter, onClick: (EndfieldCharacter) -> Unit) {
                gridBinding?.let { bindGrid(it, character, onClick); return }
                val binding = requireNotNull(listBinding)
                binding.root.setOnClickListener { onClick(character) }
                val data = character.charData
                binding.Name.text = data?.name?.takeIf(String::isNotBlank)
                    ?: character.id.orEmpty()
                bindRarity(binding.RarityStripe, character)
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
                binding.Progress.text = listOfNotNull(
                    character.level?.let { "Lv.$it" },
                    character.evolvePhase?.let { binding.root.context.getString(R.string.endfield_char_evolve, it) },
                    character.potentialLevel?.let { "潜能 $it" },
                ).joinToString(" · ")
                binding.Avatar.load(data?.avatarSqUrl?.takeIf(String::isNotBlank)) {
                    placeholder(R.drawable.ic_user)
                    error(R.drawable.ic_user)
                }
            }
        }
    }
}
