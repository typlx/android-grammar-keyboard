package com.typlx.keyboard

import android.content.Context

/**
 * Facade that delegates to focused preference classes.
 *
 * Use [apiConfig], [ui], or [features] directly when only one domain is needed.
 * All properties are also accessible here for backward-compatible call sites.
 */
class PreferencesManager(context: Context) {

    companion object {
        // Re-exported from sub-classes so existing call sites compile without changes.
        const val KEY_HAPTIC = FeaturePreferences.KEY_HAPTIC
        const val KEY_AUTO_SUGGEST = FeaturePreferences.KEY_AUTO_SUGGEST
        const val KEY_CRASH_REPORTING = FeaturePreferences.KEY_CRASH_REPORTING
        const val KEY_DOUBLE_SPACE_PERIOD = FeaturePreferences.KEY_DOUBLE_SPACE_PERIOD
        const val KEY_AUTO_CAP = FeaturePreferences.KEY_AUTO_CAP

        const val KEY_THEME_PRESET = UiPreferences.KEY_THEME_PRESET
        const val KEY_CORNER_RADIUS_DP = UiPreferences.KEY_CORNER_RADIUS_DP
        const val KEY_KEY_ALPHA_PERCENT = UiPreferences.KEY_KEY_ALPHA_PERCENT
        const val KEY_KEYBOARD_LAYOUT = UiPreferences.KEY_KEYBOARD_LAYOUT
        const val KEY_KEY_SIZE_PRESET = UiPreferences.KEY_KEY_SIZE_PRESET
        const val KEY_SHOW_NUMBER_ROW = UiPreferences.KEY_SHOW_NUMBER_ROW
    }

    val apiConfig = ApiConfigPreferences(context)
    val ui = UiPreferences(context)
    val features = FeaturePreferences(context)

    // API config
    var apiUrl: String by apiConfig::apiUrl
    var model: String by apiConfig::model
    var apiToken: String by apiConfig::apiToken
    val isConfigured: Boolean get() = apiConfig.isConfigured

    // Feature toggles
    var hapticFeedbackEnabled: Boolean by features::hapticFeedbackEnabled
    var autoSuggestEnabled: Boolean by features::autoSuggestEnabled
    var crashReportingEnabled: Boolean by features::crashReportingEnabled
    var doubleSpacePeriodEnabled: Boolean by features::doubleSpacePeriodEnabled
    var autoCapEnabled: Boolean by features::autoCapEnabled

    // UI / layout
    var themePreset by ui::themePreset
    var cornerRadiusDp: Int by ui::cornerRadiusDp
    var keyAlphaPercent: Int by ui::keyAlphaPercent
    var keyboardLayoutId by ui::keyboardLayoutId
    var keySizePreset by ui::keySizePreset
    var showNumberRow: Boolean by ui::showNumberRow
}
