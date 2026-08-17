package com.blueskybone.arkscreen.ui.common

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.core.logger.LogRepository
import com.blueskybone.arkscreen.databinding.ItemLogFileBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogFileAdapter(
    private val onClick: (LogRepository.Entry) -> Unit,
    private val onMore: (View, LogRepository.Entry) -> Unit,
) : ListAdapter<LogRepository.Entry, LogFileAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemLogFileBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClick,
            onMore,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLogFileBinding,
        private val onClick: (LogRepository.Entry) -> Unit,
        private val onMore: (View, LogRepository.Entry) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

        fun bind(entry: LogRepository.Entry) {
            binding.Type.text = entry.type.displayName
            binding.Name.text = entry.file.name
            binding.Meta.text = buildString {
                append(dateFormat.format(Date(entry.modifiedAt)))
                append("  ·  ")
                append(Formatter.formatShortFileSize(binding.root.context, entry.size))
            }
            binding.root.setOnClickListener { onClick(entry) }
            binding.More.setOnClickListener { onMore(it, entry) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<LogRepository.Entry>() {
            override fun areItemsTheSame(
                oldItem: LogRepository.Entry,
                newItem: LogRepository.Entry,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: LogRepository.Entry,
                newItem: LogRepository.Entry,
            ): Boolean = oldItem == newItem
        }
    }
}
