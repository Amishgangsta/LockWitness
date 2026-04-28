package com.lockwitness.app.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeVerificationChecklistTest {
    @Test
    fun checklistContainsRequiredRuntimeItems() {
        val items = RuntimeVerificationChecklist.items

        assertTrue(items.contains("Device Admin activation test"))
        assertTrue(items.contains("Wrong unlock test"))
        assertTrue(items.contains("Photo test"))
        assertTrue(items.contains("Video test"))
        assertTrue(items.contains("Location test"))
        assertTrue(items.contains("History/detail test"))
        assertTrue(items.contains("Export test"))
        assertTrue(items.contains("Share chooser test"))
        assertTrue(items.contains("Free/Pro gate test"))
    }

    @Test
    fun markdownUsesChecklistFormat() {
        val markdown = RuntimeVerificationChecklist.asMarkdown()

        assertTrue(markdown.startsWith("# Runtime Verification Checklist"))
        assertTrue(markdown.contains("- [ ] Device Admin activation test"))
        assertEquals(RuntimeVerificationChecklist.items.size, markdown.lines().count { it.startsWith("- [ ]") })
    }
}
