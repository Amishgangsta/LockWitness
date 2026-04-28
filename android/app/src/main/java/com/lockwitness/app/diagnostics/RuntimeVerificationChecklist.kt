package com.lockwitness.app.diagnostics

object RuntimeVerificationChecklist {
    val items: List<String> = listOf(
        "Device Admin activation test",
        "Wrong unlock test",
        "Photo test",
        "Video test",
        "Location test",
        "History/detail test",
        "Export test",
        "Share chooser test",
        "Free/Pro gate test"
    )

    fun asMarkdown(): String =
        buildString {
            appendLine("# Runtime Verification Checklist")
            items.forEach { item ->
                appendLine("- [ ] $item")
            }
        }
}
