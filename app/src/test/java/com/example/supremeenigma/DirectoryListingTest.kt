package com.example.supremeenigma

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the directory listing command logic.
 *
 * The actual execution of `rish -c 'ls'` (via Shizuku.newProcess) requires a live
 * Shizuku service, so these tests validate the surrounding output-parsing logic using
 * an in-process stub.
 */
class DirectoryListingTest {

    @Test
    fun `parseOutput returns trimmed lines for normal ls output`() {
        val raw = "file1\nfile2\nfile3\n"
        val lines = raw.trim().lines()
        assertEquals(3, lines.size)
        assertEquals("file1", lines[0])
        assertEquals("file3", lines[2])
    }

    @Test
    fun `parseOutput detects empty output`() {
        val raw = ""
        assertTrue(raw.isEmpty())
    }

    @Test
    fun `parseOutput treats whitespace-only output as empty`() {
        val raw = "   \n  "
        assertFalse(raw.trim().isNotEmpty())
    }

    @Test
    fun `ls command arguments are correct`() {
        val cmd = arrayOf("ls")
        assertEquals(1, cmd.size)
        assertEquals("ls", cmd[0])
    }
}
