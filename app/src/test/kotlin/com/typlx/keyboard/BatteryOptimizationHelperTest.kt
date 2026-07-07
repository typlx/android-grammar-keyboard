package com.typlx.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryOptimizationHelperTest {

    @Test
    fun `batteryExemptionUri formats package scheme correctly`() {
        assertEquals("package:com.typlx.keyboard", batteryExemptionUri("com.typlx.keyboard"))
    }

    @Test
    fun `batteryExemptionUri uses the supplied package name verbatim`() {
        val pkg = "com.example.myapp"
        val uri = batteryExemptionUri(pkg)
        assertTrue(uri.startsWith("package:"))
        assertTrue(uri.endsWith(pkg))
    }

    @Test
    fun `batteryExemptionUri does not double-encode the package prefix`() {
        val uri = batteryExemptionUri("com.test.app")
        assertFalse(uri.contains("package:package:"))
    }

    @Test
    fun `batteryExemptionUri length equals prefix plus package name`() {
        val pkg = "com.typlx.keyboard"
        assertEquals("package:".length + pkg.length, batteryExemptionUri(pkg).length)
    }
}
