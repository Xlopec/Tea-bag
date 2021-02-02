package com.max.reader.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.view.inputmethod.InputMethodManager

inline val Context.isDarkModeEnabled: Boolean
    get() = UI_MODE_NIGHT_YES == resources.configuration.uiMode and UI_MODE_NIGHT_MASK

fun hideKeyboardFrom(
    context: Context,
    view: View,
) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
