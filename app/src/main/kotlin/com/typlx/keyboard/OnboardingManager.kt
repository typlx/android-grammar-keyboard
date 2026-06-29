package com.typlx.keyboard

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

private const val ONBOARDING_PREFS = "typlx_onboarding"
private const val KEY_COMPLETE = "onboarding_complete"

/**
 * Pure function: returns true if [defaultIme] identifies our package as the default IME.
 * Extracted for unit-testability without an Android Context.
 */
internal fun defaultImeMatchesPackage(defaultIme: String?, ourPackage: String): Boolean {
    if (defaultIme.isNullOrBlank()) return false
    return defaultIme.startsWith("$ourPackage/")
}

class OnboardingManager(private val context: Context) {

    fun isComplete(): Boolean =
        context.getSharedPreferences(ONBOARDING_PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETE, false)

    fun markComplete() {
        context.getSharedPreferences(ONBOARDING_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_COMPLETE, true).apply()
    }

    fun isImeEnabled(): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val ourPkg = context.packageName
        return imm.enabledInputMethodList.any { it.packageName == ourPkg }
    }

    fun isImeDefault(): Boolean {
        val default = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        )
        return defaultImeMatchesPackage(default, context.packageName)
    }
}
