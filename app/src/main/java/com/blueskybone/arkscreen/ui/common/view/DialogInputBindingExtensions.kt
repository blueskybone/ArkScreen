package com.blueskybone.arkscreen.ui.common.view

import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.google.android.material.textfield.TextInputLayout

fun DialogInputBinding.configureSingleInput(
    hint: CharSequence,
    sensitive: Boolean = false,
) {
    InputLayout1.hint = hint
    InputLayout2.visibility = View.GONE
    EditText1.inputType = InputType.TYPE_CLASS_TEXT or
        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
        if (sensitive) InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS else 0
    EditText1.imeOptions = if (sensitive) {
        EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
    } else {
        EditorInfo.IME_ACTION_DONE
    }
}

fun DialogInputBinding.configureLinkInput(
    titleHint: CharSequence,
    urlHint: CharSequence,
) {
    InputLayout1.hint = titleHint
    InputLayout2.hint = urlHint
    InputLayout2.visibility = View.VISIBLE
    InputLayout2.endIconMode = TextInputLayout.END_ICON_NONE
    EditText1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    EditText1.imeOptions = EditorInfo.IME_ACTION_NEXT
    EditText2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
    EditText2.imeOptions = EditorInfo.IME_ACTION_DONE
}

fun DialogInputBinding.configurePasswordLogin(
    phoneHint: CharSequence,
    passwordHint: CharSequence,
) {
    InputLayout1.hint = phoneHint
    InputLayout2.hint = passwordHint
    InputLayout2.visibility = View.VISIBLE
    InputLayout2.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

    EditText1.inputType = InputType.TYPE_CLASS_PHONE
    EditText1.imeOptions = EditorInfo.IME_ACTION_NEXT
    EditText1.setAutofillHints(View.AUTOFILL_HINT_PHONE)

    EditText2.inputType =
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    EditText2.imeOptions =
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
    EditText2.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
}
