/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.metrolist.android.utils.AppFontManager
import com.metrolist.android.utils.QuickFontManager
import com.metrolist.shared.settings.FontOption
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.theme.MetrolistTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun getAppTypography(): Typography {
    val context = LocalContext.current
    val appFont = Settings.shared.appFont
    val customFontFile = Settings.shared.androidCustomFontFile
    val activeFontId = Settings.shared.androidActiveFontId
    val font by produceState<FontFamily>(FontFamily.Default, appFont, customFontFile, activeFontId) {
        value = withContext(Dispatchers.IO) { resolveFontFamily(context, appFont, customFontFile, activeFontId) }
    }
    val typography = MetrolistTypography()
    val weightAdjustment = Settings.shared.appFontWeightAdjustment

    return Typography(
        fontFamily = font,
        displayLarge =
            typography.displayLarge.copy(
                fontFamily = font,
                fontWeight = typography.displayLarge.fontWeight.adjusted(weightAdjustment),
            ),
        displayMedium =
            typography.displayMedium.copy(
                fontFamily = font,
                fontWeight = typography.displayMedium.fontWeight.adjusted(weightAdjustment),
            ),
        displaySmall =
            typography.displaySmall.copy(
                fontFamily = font,
                fontWeight = typography.displaySmall.fontWeight.adjusted(weightAdjustment),
            ),
        headlineLarge =
            typography.headlineLarge.copy(
                fontFamily = font,
                fontWeight = typography.headlineLarge.fontWeight.adjusted(weightAdjustment),
            ),
        headlineMedium =
            typography.headlineMedium.copy(
                fontFamily = font,
                fontWeight = typography.headlineMedium.fontWeight.adjusted(weightAdjustment),
            ),
        headlineSmall =
            typography.headlineSmall.copy(
                fontFamily = font,
                fontWeight = typography.headlineSmall.fontWeight.adjusted(weightAdjustment),
            ),
        titleLarge =
            typography.titleLarge.copy(
                fontFamily = font,
                fontWeight = typography.titleLarge.fontWeight.adjusted(weightAdjustment),
            ),
        titleMedium =
            typography.titleMedium.copy(
                fontFamily = font,
                fontWeight = typography.titleMedium.fontWeight.adjusted(weightAdjustment),
            ),
        titleSmall =
            typography.titleSmall.copy(
                fontFamily = font,
                fontWeight = typography.titleSmall.fontWeight.adjusted(weightAdjustment),
            ),
        bodyLarge =
            typography.bodyLarge.copy(
                fontFamily = font,
                fontWeight = typography.bodyLarge.fontWeight.adjusted(weightAdjustment),
            ),
        bodyMedium =
            typography.bodyMedium.copy(
                fontFamily = font,
                fontWeight = typography.bodyMedium.fontWeight.adjusted(weightAdjustment),
            ),
        bodySmall =
            typography.bodySmall.copy(
                fontFamily = font,
                fontWeight = typography.bodySmall.fontWeight.adjusted(weightAdjustment),
            ),
        labelLarge =
            typography.labelLarge.copy(
                fontFamily = font,
                fontWeight = typography.labelLarge.fontWeight.adjusted(weightAdjustment),
            ),
        labelMedium =
            typography.labelMedium.copy(
                fontFamily = font,
                fontWeight = typography.labelMedium.fontWeight.adjusted(weightAdjustment),
            ),
        labelSmall =
            typography.labelSmall.copy(
                fontFamily = font,
                fontWeight = typography.labelSmall.fontWeight.adjusted(weightAdjustment),
            ),
    )
}

private fun FontWeight?.adjusted(adjustment: Int): FontWeight? =
    this?.let { FontWeight((it.weight + adjustment).coerceIn(100, 900)) }

private fun resolveFontFamily(
    context: Context,
    appFont: FontOption,
    customFontFile: String,
    activeFontId: String,
): FontFamily {
    when (activeFontId) {
        QuickFontManager.SYSTEM_ID -> {
            return FontFamily.Default
        }

        QuickFontManager.CUSTOM_ID -> {
            return AppFontManager.loadFontFamily(context, customFontFile)
        }

        else -> {
            if (QuickFontManager.isSupportedId(activeFontId)) {
                return QuickFontManager.loadFontFamily(context, activeFontId)
            }
        }
    }

    if (activeFontId.isNotEmpty()) return FontFamily.Default

    if (appFont == FontOption.GOOGLE_SANS) {
        return QuickFontManager.loadFontFamily(context, QuickFontManager.GOOGLE_SANS_ID)
    }

    if (customFontFile.isNotBlank()) {
        val customFont = AppFontManager.loadFontFamily(context, customFontFile)
        if (customFont != FontFamily.Default) return customFont
    }

    return FontFamily.Default
}
