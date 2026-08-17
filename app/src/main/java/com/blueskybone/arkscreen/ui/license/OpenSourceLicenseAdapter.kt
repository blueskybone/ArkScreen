package com.blueskybone.arkscreen.ui.license

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemOpenSourceLibraryBinding
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary

class OpenSourceLicenseAdapter(
    private val onClick: (OpenSourceLibrary) -> Unit,
) : ListAdapter<OpenSourceLibrary, OpenSourceLicenseAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOpenSourceLibraryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemOpenSourceLibraryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onClick(getItem(position))
            }
        }

        fun bind(item: OpenSourceLibrary) {
            binding.LibraryName.text = item.name.ifBlank { item.id.substringAfter(':') }
            binding.LibraryVersion.text = item.version
            binding.LibraryLicense.text = item.licenses
                .map { it.name.ifBlank { binding.root.context.getString(R.string.license_unknown) } }
                .distinct()
                .joinToString()
                .ifBlank { binding.root.context.getString(R.string.license_unknown) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OpenSourceLibrary>() {
        override fun areItemsTheSame(
            oldItem: OpenSourceLibrary,
            newItem: OpenSourceLibrary,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: OpenSourceLibrary,
            newItem: OpenSourceLibrary,
        ): Boolean = oldItem == newItem
    }
}
