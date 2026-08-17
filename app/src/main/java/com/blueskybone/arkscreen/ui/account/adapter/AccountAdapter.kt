package com.blueskybone.arkscreen.ui.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemAccountBinding
import com.blueskybone.arkscreen.ui.account.model.AccountItemAction
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */


class AccountAdapter(
    private val action: AccountItemAction
) : ListAdapter<AccountItemUiModel, AccountAdapter.AccountVH>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AccountItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: AccountItemUiModel,
                newItem: AccountItemUiModel
            ): Boolean {
                val oldAccount = oldItem.account
                val newAccount = newItem.account
                return oldAccount.uid == newAccount.uid &&
                        oldAccount::class == newAccount::class
            }

            override fun areContentsTheSame(
                oldItem: AccountItemUiModel,
                newItem: AccountItemUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountVH {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountVH(binding)
    }

    override fun onBindViewHolder(holder: AccountVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountVH(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItemSafely()
                if (item != null) {
                    action.onClick(item.account)
                }
            }

            binding.root.setOnLongClickListener {
                val item = getItemSafely()
                if (item != null) {
                    action.onLongClick(item.account)
                    true
                } else {
                    false
                }
            }
        }

        fun bind(item: AccountItemUiModel) {
            val account = item.account

            binding.Title.text = account.nickName
            binding.Value.text = binding.root.context.getString(R.string.uid_info, account.uid)

            binding.Icon.setImageResource(
                if (account.official) R.drawable.hg_icon_80x80
                else R.drawable.bili_icon_75x71
            )

            binding.Checked.visibility =
                if (item.isDefault) View.VISIBLE else View.GONE
        }

        private fun getItemSafely(): AccountItemUiModel? {
            val position = bindingAdapterPosition
            return if (position != RecyclerView.NO_POSITION) getItem(position) else null
        }
    }
}