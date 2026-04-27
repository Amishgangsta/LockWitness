package com.lockwitness.app.photo

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class Sha256HasherTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun hashReturnsExpectedSha256() {
        val file = temporaryFolder.newFile("sample.txt").apply {
            writeText("LockWitness")
        }

        assertEquals(
            "a3bb17c62380de07159e9f48e219384487696902479d286a1c374cf3e8434849",
            Sha256Hasher.hash(file)
        )
    }
}
