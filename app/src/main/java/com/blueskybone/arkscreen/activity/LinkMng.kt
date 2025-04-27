package com.blueskybone.arkscreen.activity

import android.content.Intent
import androidx.activity.viewModels
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.recyclerview.ItemListener
import com.blueskybone.arkscreen.recyclerview.LinkAdapter
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.room.Type
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */
class LinkMng : ListActivity(Type.LINK), ItemListener {
    private val model: BaseModel by viewModels()
    private var adapter: LinkAdapter? = null
    override fun setUpRecycler() {
        adapter = LinkAdapter(this)
        binding.RecyclerView.adapter = adapter
        model.links.observe(this) { value ->
            adapter?.submitList(value)
        }
    }

    override fun onAddButtonClick() {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.setText(R.string.prefix)
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(R.string.add_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    model.insertLink(Link(title = title, url = url))
                }
            }.show()
    }

    override fun onLongClick(position: Int) {
        adapter?.currentList?.get(position)?.let { value ->
            MenuDialog(this)
                .add(R.string.edit) { displayEditDialog(value) }
                .add(R.string.delete) { confirmDeletion(value) }
                .show()
        }
    }

    private fun confirmDeletion(value: Link) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteLink(value) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun displayEditDialog(value: Link) {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText1.setText(value.title)
        dialogBinding.EditText2.setText(value.url)
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(R.string.edit_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    value.title = title
                    value.url = url
                    model.updateLink(value)
                }
            }.show()
    }

    override fun onActivityResult(data: Intent?) {

    }

    override fun onClick(position: Int) {

    }
}
