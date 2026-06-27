package com.typlx.keyboard.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftStateTest {

    private val t0 = 1_000_000L

    @Test
    fun `off to shift_once on first tap`() {
        val (state, ms) = nextShiftState(ShiftState.OFF, t0, 0L)
        assertEquals(ShiftState.SHIFT_ONCE, state)
        assertEquals(t0, ms)
    }

    @Test
    fun `shift_once to caps_lock on fast double-tap`() {
        val firstTap = t0
        val secondTap = t0 + 200L // 200ms < 400ms threshold
        val (state, _) = nextShiftState(ShiftState.SHIFT_ONCE, secondTap, firstTap)
        assertEquals(ShiftState.CAPS_LOCK, state)
    }

    @Test
    fun `shift_once stays shift_once on slow second tap`() {
        val firstTap = t0
        val secondTap = t0 + 500L // 500ms > 400ms threshold
        val (state, ms) = nextShiftState(ShiftState.SHIFT_ONCE, secondTap, firstTap)
        assertEquals(ShiftState.SHIFT_ONCE, state)
        assertEquals(secondTap, ms) // timer resets to the new tap
    }

    @Test
    fun `caps_lock to off on tap`() {
        val (state, _) = nextShiftState(ShiftState.CAPS_LOCK, t0, t0 - 1000L)
        assertEquals(ShiftState.OFF, state)
    }

    @Test
    fun `double-tap exactly at 399ms boundary activates caps_lock`() {
        val firstTap = t0
        val secondTap = t0 + 399L
        val (state, _) = nextShiftState(ShiftState.SHIFT_ONCE, secondTap, firstTap)
        assertEquals(ShiftState.CAPS_LOCK, state)
    }

    @Test
    fun `double-tap exactly at 400ms boundary does not activate caps_lock`() {
        val firstTap = t0
        val secondTap = t0 + 400L
        val (state, _) = nextShiftState(ShiftState.SHIFT_ONCE, secondTap, firstTap)
        assertEquals(ShiftState.SHIFT_ONCE, state)
    }

    @Test
    fun `slow tap resets timer so third fast tap also activates caps_lock`() {
        // First tap: OFF → SHIFT_ONCE, timer = t0
        val (s1, ms1) = nextShiftState(ShiftState.OFF, t0, 0L)
        assertEquals(ShiftState.SHIFT_ONCE, s1)
        // Second tap slow: SHIFT_ONCE → SHIFT_ONCE, timer = t0 + 600
        val (s2, ms2) = nextShiftState(s1, t0 + 600L, ms1)
        assertEquals(ShiftState.SHIFT_ONCE, s2)
        // Third tap fast after reset: SHIFT_ONCE → CAPS_LOCK
        val (s3, _) = nextShiftState(s2, t0 + 750L, ms2)
        assertEquals(ShiftState.CAPS_LOCK, s3)
    }
}
