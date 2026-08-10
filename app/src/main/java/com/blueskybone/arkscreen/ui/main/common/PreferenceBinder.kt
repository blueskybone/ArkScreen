package com.blueskybone.arkscreen.ui.main.common

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceSubSwitchBinding
import com.blueskybone.arkscreen.databinding.PreferenceSubValueBinding
import com.blueskybone.arkscreen.databinding.PreferenceSwitchBinding
import com.blueskybone.arkscreen.databinding.PreferenceValueBinding
import com.blueskybone.arkscreen.ui.common.bindinginfo.ListInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.TextInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder


object PreferenceBinder {


    fun bindSwitch(
        binding: PreferenceSwitchBinding,
        icon: Int?,
        text: Int,
        pref: Preference<Boolean>
    ) {
        binding.setUp(icon, text, pref)
    }

    fun bindSubValue(
        binding: PreferenceSubValueBinding,
        context: Context,
        icon: Int?,
        textInfo: TextInfo,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)? = null
    ) {
        binding.setUp(context, icon, textInfo, listInfo, pref, onClick)
    }

    fun bindPreferenceValue(
        binding: PreferenceValueBinding,
        context: Context,
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)? = null
    ) {
        binding.setUp(context, icon, listInfo, pref, onClick)
    }

    fun bindPreference(
        binding: PreferenceBinding,
        context: Context,
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)? = null
    ) {
        binding.setUp(context, icon, listInfo, pref, onClick)
    }

    fun bindSubSwitch(
        binding: PreferenceSubSwitchBinding,
        icon: Int?,
        textInfo: TextInfo,
        pref: Preference<Boolean>,
        onCall: (() -> Unit)? = null,
        offCall: (() -> Unit)? = null
    ) {
        binding.setUp(icon, textInfo, pref, onCall, offCall)
    }

    fun bindPreferenceText(
        binding: PreferenceBinding,
        icon: Int?,
        textInfo: TextInfo
    ) {
        binding.setUp(icon, textInfo)
    }

    fun bindPreferenceText(
        binding: PreferenceBinding,
        icon: Int?,
        text: Int
    ) {
        binding.Icon.bindIcon(icon)
        binding.Title.setText(text)
        binding.Value.visibility = View.GONE
    }



    private fun ImageView.bindIcon(icon: Int?) {
        if (icon == null) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            setImageResource(icon)
        }
    }

    private data class ChoiceState(
        val entries: Array<String>,
        val entryValues: Array<String>,
        val checkedIndex: Int
    ) {
        fun displayText(): String {
            if (entries.isEmpty()) return "未知"
            return entries.getOrElse(checkedIndex) { entries.first() }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ChoiceState

            if (checkedIndex != other.checkedIndex) return false
            if (!entries.contentEquals(other.entries)) return false
            if (!entryValues.contentEquals(other.entryValues)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = checkedIndex
            result = 31 * result + entries.contentHashCode()
            result = 31 * result + entryValues.contentHashCode()
            return result
        }
    }

    private fun buildChoiceState(
        context: Context,
        listInfo: ListInfo,
        currentValue: String
    ): ChoiceState {
        val entries = listInfo.getEntries(context)
        val entryValues = listInfo.getEntryValues()

        val safeIndex = entryValues.indexOf(currentValue)
            .takeIf { it >= 0 && it < entries.size }
            ?: 0

        return ChoiceState(
            entries = entries,
            entryValues = entryValues,
            checkedIndex = safeIndex
        )
    }

    private fun showSingleChoiceDialog(
        context: Context,
        title: Int,
        state: ChoiceState,
        onSelected: (index: Int) -> Unit
    ) {
        if (state.entries.isEmpty() || state.entryValues.isEmpty()) return

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(state.entries, state.checkedIndex) { dialog, which ->
                dialog.dismiss()
                onSelected(which)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun PreferenceSwitchBinding.setUp(
        icon: Int?,
        text: Int,
        pref: Preference<Boolean>
    ) {
        Icon.bindIcon(icon)
        Title.setText(text)

        Switch.setOnCheckedChangeListener(null)
        Switch.isChecked = pref.get()
        Switch.setOnCheckedChangeListener { _, isChecked ->
            pref.set(isChecked)
        }
    }

    private fun bindListPreference(
        context: Context,
        iconView: ImageView?,
        titleView: TextView,
        valueView: TextView,
        root: View,
        icon: Int?,
        titleRes: Int,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)? = null
    ) {
        iconView?.bindIcon(icon)
        titleView.setText(titleRes)

        var state = buildChoiceState(context, listInfo, pref.get())
        valueView.text = state.displayText()

        root.setOnClickListener {
            showSingleChoiceDialog(context, listInfo.title, state) { which ->
                val newValue = state.entryValues.getOrNull(which) ?: return@showSingleChoiceDialog
                val newText = state.entries.getOrNull(which) ?: return@showSingleChoiceDialog

                pref.set(newValue)
                valueView.text = newText
                state = state.copy(checkedIndex = which)
                onClick?.invoke()
            }
        }
    }


    private fun PreferenceSubValueBinding.setUp(
        context: Context,
        icon: Int?,
        textInfo: TextInfo,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        Icon.bindIcon(icon)
        Title.setText(textInfo.title)
        SubTitle.setText(textInfo.subTitle)

        bindListPreference(
            context = context,
            iconView = null,
            valueView = Value,
            root = root,
            icon = null,
            listInfo = listInfo,
            pref = pref,
            onClick = onClick
        )
    }

    private fun bindListPreference(
        context: Context,
        iconView: ImageView?,
        valueView: TextView,
        root: View,
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)? = null
    ) {
        iconView?.bindIcon(icon)

        var state = buildChoiceState(context, listInfo, pref.get())
        valueView.text = state.displayText()

        root.setOnClickListener {
            showSingleChoiceDialog(context, listInfo.title, state) { which ->
                val newValue = state.entryValues.getOrNull(which) ?: return@showSingleChoiceDialog
                val newText = state.entries.getOrNull(which) ?: return@showSingleChoiceDialog

                pref.set(newValue)
                valueView.text = newText
                state = state.copy(checkedIndex = which)
                onClick?.invoke()
            }
        }
    }
    private fun PreferenceBinding.setUp(icon: Int?, textInfo: TextInfo) {
        Icon.bindIcon(icon)
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)
        Value.visibility = View.VISIBLE
    }

    private fun PreferenceBinding.setUp(
        context: Context,
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        Icon.bindIcon(icon)
        Title.setText(listInfo.title)

        bindListPreference(
            context = context,
            iconView = null,
            valueView = Value,
            root = root,
            icon = null,
            listInfo = listInfo,
            pref = pref,
            onClick = onClick
        )
    }

    private fun PreferenceValueBinding.setUp(
        context: Context,
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        Icon.bindIcon(icon)
        Title.setText(listInfo.title)

        bindListPreference(
            context = context,
            iconView = null,
            valueView = Value,
            root = root,
            icon = null,
            listInfo = listInfo,
            pref = pref,
            onClick = onClick
        )
    }

    private fun PreferenceSubSwitchBinding.setUp(
        icon: Int?,
        textInfo: TextInfo,
        pref: Preference<Boolean>,
        onCall: (() -> Unit)?,
        offCall: (() -> Unit)?,
    ) {
        Icon.bindIcon(icon)
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)

        Switch.setOnCheckedChangeListener(null)
        Switch.isChecked = pref.get()
        Switch.setOnCheckedChangeListener { _, isChecked ->
            pref.set(isChecked)
            if (isChecked) onCall?.invoke() else offCall?.invoke()
        }
    }
}
