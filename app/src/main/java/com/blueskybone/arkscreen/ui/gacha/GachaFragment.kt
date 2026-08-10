package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.FragmentGachaBinding
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.account.common.AccountPickerPopup
import com.blueskybone.arkscreen.ui.account.model.AccountItemAction
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.gacha.adapter.GachaAdapter
import com.blueskybone.arkscreen.ui.gacha.model.GachaPool
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiSnapshot
import kotlinx.coroutines.launch

class GachaFragment : Fragment() {

    private val model: GachaModel by activityViewModel()
    private val settingPrefManager: SettingPrefManager by inject()
    private var adapter: GachaAdapter? = null
    private var accountPopup: AccountPickerPopup? = null
    private var adapterAccount: AccountAdapter? = null
    private var gachaPools: List<GachaPool> = emptyList()
    private var _binding: FragmentGachaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaBinding.inflate(inflater)
        adapterAccount = AccountAdapter(createItemAction())
        setupBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiState()
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    renderAccount(state)
                    state.gachaUiSnapshot?.let{
                        renderGachaOverview(it)
                        renderGachaPool(it)
                    }
                }
            }
        }
    }

    private fun renderAccount(state: GachaUiState) {
        val current = state.currAccount
        binding.NickName.text = current?.nickName.orEmpty()
        if (current != null) {
            binding.Icon.setImageResource(
                if (current.official) R.drawable.hg_icon_80x80
                else R.drawable.bili_icon_75x71
            )
        } else {
            binding.Icon.setImageDrawable(null)
        }

        adapterAccount?.submitList(
            state.accList.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = account.uid == current?.uid,
                )
            }
        )
    }



    private fun renderGachaOverview(gachaUi: GachaUiSnapshot) {
        binding.CountSum.text = getString(R.string.gacha_count, gachaUi.gachaOverview.totalCount)
        val rarity6cnt = gachaUi.gachaSummary.rare6Count
        binding.Rarity6.text = rarity6cnt.toString()
        binding.AverageCount.text =
            if (rarity6cnt == 0) "-" else getString(
                R.string.gacha_count,
                gachaUi.gachaOverview.totalCount / rarity6cnt
            )
        binding.NormalCount.text =
            getString(R.string.gacha_count, gachaUi.gachaOverview.poolCountNormal)
        binding.FesCount.text = getString(R.string.gacha_count, gachaUi.gachaOverview.poolCountFes)
        binding.CoreCount.text =
            getString(R.string.gacha_count, gachaUi.gachaOverview.poolCountCore)
        binding.DateRange.text = getString(R.string.date_range, gachaUi.gachaOverview.dateRange)
    }

    private fun renderGachaPool(gachaUi: GachaUiSnapshot) {
        gachaPools = gachaUi.gachaPools
        submitVisiblePools()
    }

    private fun submitVisiblePools() {
        val visiblePools = if (binding.ShowEmptyPools.isChecked) {
            gachaPools
        } else {
            gachaPools.filter { it.hitRecords.isNotEmpty() }
        }
        adapter?.submitList(visiblePools)
    }

    private fun createItemAction(): AccountItemAction {
        return object : AccountItemAction {
            override fun onClick(account: Account) {
                model.checkoutAccount(account)
                accountPopup?.dismiss()
            }
            override fun onLongClick(account: Account) {
            }
        }
    }


    private fun setupBinding() {
        adapter = GachaAdapter(requireContext())
        binding.RecyclerView.adapter = adapter
        binding.ShowEmptyPools.isChecked = settingPrefManager.showEmptyGachaPools.get()
        binding.RecyclerView.apply {
            // 设置固定大小，优化性能
            setHasFixedSize(true)

            // 禁用滚动
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        binding.Exchange.setOnClickListener { view ->
            showAccountPopup(view)
        }
        binding.PlayerInfo.setOnClickListener { view ->
            showAccountPopup(view)
        }
        binding.ShowEmptyPools.setOnCheckedChangeListener { _, isChecked ->
            settingPrefManager.showEmptyGachaPools.set(isChecked)
            submitVisiblePools()
        }
    }

    private fun showAccountPopup(anchor: View) {
        if (accountPopup?.isShowing == true) return
        val accountAdapter = adapterAccount ?: return
        accountPopup = AccountPickerPopup(requireActivity(), accountAdapter).also {
            it.show(anchor)
        }
    }

    override fun onDestroyView() {
        accountPopup?.dismiss()
        accountPopup = null
        binding.RecyclerView.adapter = null
        adapter = null
        adapterAccount = null
        gachaPools = emptyList()
        _binding = null
        super.onDestroyView()
    }
}
