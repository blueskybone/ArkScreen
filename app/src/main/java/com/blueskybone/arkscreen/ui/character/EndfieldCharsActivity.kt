package com.blueskybone.arkscreen.ui.character

import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldCharsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.Characters.adapter = adapter
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
        adapter.submitList(state.characters)
        binding.Summary.text = when {
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.loading -> getString(R.string.endfield_chars_loading)
            state.error != null -> state.error
            state.characters.isEmpty() -> getString(R.string.endfield_chars_empty)
            else -> getString(R.string.endfield_chars_count, state.characters.size)
        }
    }

    private fun chooseAccount() {
        val accounts = currentState.accounts
        if (accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.endfield_choose_account)
            .setSingleChoiceItems(
                accounts.map { it.nickName }.toTypedArray(),
                accounts.indexOfFirst { it.uid == currentState.selectedUid },
            ) { dialog, index ->
                model.select(accounts[index].uid)
                dialog.dismiss()
            }
            .show()
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
                binding.Traits.text = listOfNotNull(
                    data?.rarity?.value?.takeIf(String::isNotBlank),
                    data?.profession?.value?.takeIf(String::isNotBlank),
                    data?.property?.value?.takeIf(String::isNotBlank),
                ).joinToString(" · ")
                binding.Progress.text = listOfNotNull(
                    character.level?.let { "Lv.$it" },
                    character.evolvePhase?.let { "晋升 $it" },
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
