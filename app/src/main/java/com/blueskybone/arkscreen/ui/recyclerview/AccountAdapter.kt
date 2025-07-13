package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemAccountBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.Account
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class AccountAdapter(private val context: Context, private val listener: ItemListener) :
    ListAdapter<Account, AccountAdapter.AccountVH>(DiffCallback) {

    private val prefManager: PrefManager by getKoin().inject()

    private object DiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.token == newItem.token
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAccountBinding.inflate(inflater, parent, false)
        return AccountVH(binding, listener)
    }

    override fun onBindViewHolder(holder: AccountVH, position: Int) {
        holder.bind(getItem(position))
    }
    inner class AccountVH(private val binding: ItemAccountBinding, listener: ItemListener) :
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

        fun bind(item: Account) {
            binding.Title.text = item.nickName
            binding.Value.text = context.getString(R.string.uid_info, item.uid)
            if (item.official) binding.Icon.setImageResource(R.drawable.hg_icon_80x80)
            else binding.Icon.setImageResource(R.drawable.bili_icon_75x71)
            binding.Checked.visibility =
                if (item.uid == prefManager.baseAccountSk.get().uid) View.VISIBLE else View.GONE
        }
    }
}