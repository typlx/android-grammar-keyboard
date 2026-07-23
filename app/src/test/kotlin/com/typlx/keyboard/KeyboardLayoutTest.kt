package com.typlx.keyboard

import org.junit.Assert.*
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun `QWERTY row1 starts with q`() {
        assertEquals("q", LAYOUT_QWERTY.row1.first())
    }

    @Test
    fun `AZERTY row1 starts with a`() {
        assertEquals("a", LAYOUT_AZERTY.row1.first())
    }

    @Test
    fun `QWERTZ row3 starts with y not z`() {
        // In QWERTZ, Z and Y are swapped compared to QWERTY
        assertEquals("y", LAYOUT_QWERTZ.row3.first())
    }

    @Test
    fun `DVORAK row2 starts with a`() {
        assertEquals("a", LAYOUT_DVORAK.row2.first())
    }

    @Test
    fun `layoutById returns correct layout`() {
        assertEquals(LAYOUT_QWERTY, layoutById(LayoutId.QWERTY))
        assertEquals(LAYOUT_AZERTY, layoutById(LayoutId.AZERTY))
        assertEquals(LAYOUT_QWERTZ, layoutById(LayoutId.QWERTZ))
        assertEquals(LAYOUT_DVORAK, layoutById(LayoutId.DVORAK))
        assertEquals(LAYOUT_CYRILLIC, layoutById(LayoutId.CYRILLIC))
    }

    @Test
    fun `ALL_LAYOUTS contains all five layouts`() {
        assertEquals(5, ALL_LAYOUTS.size)
        assertTrue(ALL_LAYOUTS.any { it.id == LayoutId.QWERTY })
        assertTrue(ALL_LAYOUTS.any { it.id == LayoutId.AZERTY })
        assertTrue(ALL_LAYOUTS.any { it.id == LayoutId.QWERTZ })
        assertTrue(ALL_LAYOUTS.any { it.id == LayoutId.DVORAK })
        assertTrue(ALL_LAYOUTS.any { it.id == LayoutId.CYRILLIC })
    }

    @Test
    fun `CYRILLIC row1 starts with Ukrainian й`() {
        assertEquals("й", LAYOUT_CYRILLIC.row1.first())
    }

    @Test
    fun `CYRILLIC has 10 keys in row1`() {
        assertEquals(10, LAYOUT_CYRILLIC.row1.size)
    }

    @Test
    fun `CYRILLIC longPressAlternatives includes є for е`() {
        val alts = LAYOUT_CYRILLIC.longPressAlternatives["е"]
            ?: throw AssertionError("Missing alternatives for 'е'")
        assertTrue(alts.contains("є"))
    }

    @Test
    fun `CYRILLIC longPressAlternatives includes ї via з`() {
        val alts = LAYOUT_CYRILLIC.longPressAlternatives["з"]
            ?: throw AssertionError("Missing alternatives for 'з'")
        assertTrue(alts.contains("ї"))
    }

    @Test
    fun `CYRILLIC longPressAlternatives includes ґ for г`() {
        val alts = LAYOUT_CYRILLIC.longPressAlternatives["г"]
            ?: throw AssertionError("Missing alternatives for 'г'")
        assertTrue(alts.contains("ґ"))
    }

    @Test
    fun `no layout has duplicate keys in a row`() {
        ALL_LAYOUTS.forEach { layout ->
            assertEquals(layout.row1.distinct(), layout.row1)
            assertEquals(layout.row2.distinct(), layout.row2)
            assertEquals(layout.row3.distinct(), layout.row3)
        }
    }

    @Test
    fun `QWERTY layout has 10 keys in row1`() {
        assertEquals(10, LAYOUT_QWERTY.row1.size)
    }

    @Test
    fun `AZERTY layout has 10 keys in row2 including m`() {
        // AZERTY row2 has m at the end (unlike QWERTY where m is in row3)
        assertEquals(10, LAYOUT_AZERTY.row2.size)
        assertTrue(LAYOUT_AZERTY.row2.contains("m"))
    }

    @Test
    fun `QWERTZ longPressAlternatives contains German umlaut for a`() {
        val alts = LAYOUT_QWERTZ.longPressAlternatives["a"] ?: throw AssertionError("Missing alternatives for 'a'")
        assertTrue(alts.contains("ä"))
    }

    @Test
    fun `AZERTY longPressAlternatives includes French accents for e`() {
        val alts = LAYOUT_AZERTY.longPressAlternatives["e"] ?: throw AssertionError("Missing alternatives for 'e'")
        assertTrue(alts.contains("é"))
        assertTrue(alts.contains("è"))
    }
}
