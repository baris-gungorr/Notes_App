package com.barisgungorr.Utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard()=(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken,
    InputMethodManager.HIDE_NOT_ALWAYS

    // klavye gizlemek için
)