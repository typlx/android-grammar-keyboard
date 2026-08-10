package com.typlx.keyboard

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GrammarRulePreferencesTest {

    private fun makeManager(storedValue: String?): PreferencesManager {
        val sharedPrefs = mock(SharedPreferences::class.java)
        `when`(sharedPrefs.getString(anyString(), nullable(String::class.java))).thenReturn(storedValue)

        val context = mock(Context::class.java)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)

        return PreferencesManager(context)
    }

    private fun makeRoundTripManager(): PreferencesManager {
        val storage = mutableMapOf<String, String?>()

        val editor = mock(SharedPreferences.Editor::class.java)
        `when`(editor.putString(anyString(), anyString())).thenAnswer { inv ->
            storage[inv.getArgument(0)] = inv.getArgument(1)
            editor
        }

        val sharedPrefs = mock(SharedPreferences::class.java)
        `when`(sharedPrefs.getString(anyString(), nullable(String::class.java))).thenAnswer { inv ->
            val key: String = inv.getArgument(0)
            val defValue: String? = inv.getArgument(1)
            if (storage.containsKey(key)) storage[key] else defValue
        }
        `when`(sharedPrefs.edit()).thenReturn(editor)

        val context = mock(Context::class.java)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)

        return PreferencesManager(context)
    }

    @Test
    fun `null stored value returns ALL — never configured`() {
        val manager = makeManager(null)
        assertEquals(GrammarRule.ALL, manager.enabledGrammarRules)
    }

    @Test
    fun `empty string stored value returns empty set — explicitly cleared`() {
        val manager = makeManager("")
        assertEquals(emptySet<GrammarRule>(), manager.enabledGrammarRules)
    }

    @Test
    fun `all four rules stored returns ALL`() {
        val stored = GrammarRule.entries.joinToString(",") { it.name }
        val manager = makeManager(stored)
        assertEquals(GrammarRule.ALL, manager.enabledGrammarRules)
    }

    @Test
    fun `single rule stored returns that rule only`() {
        val manager = makeManager("SPELLING")
        assertEquals(setOf(GrammarRule.SPELLING), manager.enabledGrammarRules)
    }

    @Test
    fun `corrupt stored value falls back to ALL`() {
        val manager = makeManager("NOT_A_RULE,ALSO_INVALID")
        assertEquals(GrammarRule.ALL, manager.enabledGrammarRules)
    }

    @Test
    fun `round-trip — write empty set then re-read yields empty set not ALL`() {
        val manager = makeRoundTripManager()
        manager.enabledGrammarRules = emptySet()
        assertEquals(emptySet<GrammarRule>(), manager.enabledGrammarRules)
    }

    @Test
    fun `round-trip — write subset then re-read yields same subset`() {
        val manager = makeRoundTripManager()
        val subset = setOf(GrammarRule.SPELLING, GrammarRule.GRAMMAR)
        manager.enabledGrammarRules = subset
        assertEquals(subset, manager.enabledGrammarRules)
    }

    @Test
    fun `round-trip — write ALL then re-read yields ALL`() {
        val manager = makeRoundTripManager()
        manager.enabledGrammarRules = GrammarRule.ALL
        assertEquals(GrammarRule.ALL, manager.enabledGrammarRules)
    }
}
