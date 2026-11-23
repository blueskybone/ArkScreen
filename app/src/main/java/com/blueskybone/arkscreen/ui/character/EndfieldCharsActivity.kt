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
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndfieldCharsActivity : AppCompatActivity() {
    private val model: EndfieldCharModel by viewModel()
    private lateinit var binding: ActivityEndfieldCharsBinding
    private val adapter = CharacterAdapter(::openDetail)
    private var currentState = EndfieldCharUiState()

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
        binding.Characters.adapter = adapter
        binding.RarityFilter.setOnClickListener { chooseFilter(0) }
        binding.ProfessionFilter.setOnClickListener { chooseFilter(1) }
        binding.PropertyFilter.setOnClickListener { chooseFilter(2) }
        supportFragmentManager.addOnBackStackChangedListener { updatePage() }
        updatePage()
        binding.AccountButton.setOnClickListener { chooseAccount() }
        binding.RefreshButton.setOnClickListener { model.refresh() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect(::render)
            }
        }
    }

    private fun render(state: EndfieldCharUiState) {
        currentState = state
        val selected = state.accounts.firstOrNull { it.uid == state.selectedUid }
        binding.AccountButton.text = selected?.nickName ?: getString(R.string.endfield_choose_account)
        binding.Loading.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.RefreshButton.isEnabled = selected != null && !state.loading
        val filtered = state.characters.filter { character ->
            (rarityFilter == null || filterValue(character, 0) == rarityFilter) &&
                (professionFilter == null || filterValue(character, 1) == professionFilter) &&
                (propertyFilter == null || filterValue(character, 2) == propertyFilter)
        }
        adapter.submitList(filtered)
        binding.Filters.visibility = if (state.characters.isNotEmpty() && !state.loading) View.VISIBLE else View.GONE
        binding.RarityFilter.text = rarityFilter ?: "稀有度"
        binding.ProfessionFilter.text = professionFilter ?: "职业"
        binding.PropertyFilter.text = propertyFilter ?: "属性"
        binding.RarityFilter.isSelected = rarityFilter != null
        binding.ProfessionFilter.isSelected = professionFilter != null
        binding.PropertyFilter.isSelected = propertyFilter != null
        binding.Summary.text = when {
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.loading -> getString(R.string.endfield_chars_loading)
            state.error != null -> state.error
            state.characters.isEmpty() -> getString(R.string.endfield_chars_empty)
            rarityFilter != null || professionFilter != null || propertyFilter != null ->
                "筛选结果 ${filtered.size} / ${state.characters.size}"
            else -> getString(R.string.endfield_chars_count, state.characters.size)
        }
    }

    private fun filterValue(character: EndfieldCharacter, kind: Int): String? = when (kind) {
        0 -> EndfieldCharDisplay.stars(character.charData?.rarity?.value, character.charData?.rarity?.key)
        1 -> character.charData?.profession?.value
        else -> character.charData?.property?.value
    }?.takeIf(String::isNotBlank)

    private fun chooseFilter(kind: Int) {
        val options = currentState.characters.mapNotNull { filterValue(it, kind) }.distinct().sortedDescending()
        val selected = when (kind) {
            0 -> rarityFilter
            1 -> professionFilter
            else -> propertyFilter
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(listOf("稀有度", "职业", "属性")[kind])
            .setSingleChoiceItems((listOf("全部") + options).toTypedArray(),
                if (selected == null) 0 else options.indexOf(selected) + 1) { dialog, index ->
                val value = options.getOrNull(index - 1)
                when (kind) {
                    0 -> rarityFilter = value
                    1 -> professionFilter = value
                    else -> propertyFilter = value
                }
                render(currentState)
                dialog.dismiss()
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
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
        accountPopup = showAccountChoices(binding.AccountButton, accounts, currentState.selectedUid) { account ->
            model.select(account.uid)
        }
    }

    private fun openDetail(character: EndfieldCharacter) {
        val id = character.id ?: character.charData?.id ?: return
        supportFragmentManager.commit {
            replace(R.id.DetailContainer, EndfieldCharDetailFragment.newInstance(id))
            addToBackStack("endfield_character")
        }
    }

    private fun updatePage() {
        val showingDetail = supportFragmentManager.backStackEntryCount > 0
        binding.ListContent.visibility = if (showingDetail) View.GONE else View.VISIBLE
        binding.DetailContainer.visibility = if (showingDetail) View.VISIBLE else View.GONE
        binding.Toolbar.setTitle(
            if (showingDetail) R.string.endfield_char_detail else R.string.operator_assets
        )
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
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
            ItemEndfieldCharBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position), onClick)
        }

        class Holder(private val binding: ItemEndfieldCharBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(character: EndfieldCharacter, onClick: (EndfieldCharacter) -> Unit) {
                binding.root.setOnClickListener { onClick(character) }
                val data = character.charData
                binding.Name.text = data?.name?.takeIf(String::isNotBlank)
                    ?: character.id.orEmpty()
                binding.RarityText.text = data?.rarity?.value.orEmpty()
                binding.RarityText.visibility = if (binding.RarityText.text.isBlank()) View.GONE else View.VISIBLE
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
