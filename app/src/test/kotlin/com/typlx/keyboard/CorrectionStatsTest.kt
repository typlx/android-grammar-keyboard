package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CorrectionStatsTest {

    private lateinit var stats: CorrectionStats

    @Before
    fun setUp() {
        stats = CorrectionStats()
    }

    @Test
    fun `initial total is zero`() {
        assertEquals(0, stats.totalCorrectionsApplied)
    }

    @Test
    fun `initial sessions is zero`() {
        assertEquals(0, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `recordCorrectionAccepted increments total`() {
        stats.recordCorrectionAccepted()
        assertEquals(1, stats.totalCorrectionsApplied)
    }

    @Test
    fun `multiple corrections accumulate`() {
        repeat(5) { stats.recordCorrectionAccepted() }
        assertEquals(5, stats.totalCorrectionsApplied)
    }

    @Test
    fun `onSessionEnd increments sessions when corrections occurred`() {
        stats.recordCorrectionAccepted()
        stats.onSessionEnd()
        assertEquals(1, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `onSessionEnd does not increment sessions when no corrections in session`() {
        stats.onSessionEnd()
        assertEquals(0, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `session count tracks multiple sessions`() {
        stats.recordCorrectionAccepted()
        stats.onSessionEnd()
        stats.recordCorrectionAccepted()
        stats.onSessionEnd()
        assertEquals(2, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `empty session after session with corrections does not increment count`() {
        stats.recordCorrectionAccepted()
        stats.onSessionEnd()
        stats.onSessionEnd()
        assertEquals(1, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `toJson serialises correctly`() {
        repeat(3) { stats.recordCorrectionAccepted() }
        stats.onSessionEnd()
        val json = stats.toJson()
        assert(json.contains("\"total\":3")) { "Expected total:3 in $json" }
        assert(json.contains("\"sessions\":1")) { "Expected sessions:1 in $json" }
    }

    @Test
    fun `loadFromJson restores state`() {
        val json = """{"total":42,"sessions":7}"""
        stats.loadFromJson(json)
        assertEquals(42, stats.totalCorrectionsApplied)
        assertEquals(7, stats.totalSessionsWithCorrections)
    }

    @Test
    fun `loadFromJson ignores malformed input`() {
        stats.loadFromJson("not json")
        assertEquals(0, stats.totalCorrectionsApplied)
    }

    @Test
    fun `roundtrip through json preserves values`() {
        repeat(10) { stats.recordCorrectionAccepted() }
        stats.onSessionEnd()
        val json = stats.toJson()

        val restored = CorrectionStats()
        restored.loadFromJson(json)
        assertEquals(10, restored.totalCorrectionsApplied)
        assertEquals(1, restored.totalSessionsWithCorrections)
    }
}
