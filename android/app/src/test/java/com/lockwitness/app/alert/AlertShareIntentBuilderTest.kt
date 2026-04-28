package com.lockwitness.app.alert

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AlertShareIntentBuilderTest {
    @Test
    fun buildSendIntentHasNoHardcodedRecipientsOrCredentials() {
        val builder = AlertShareIntentBuilder(ApplicationProvider.getApplicationContext())
        val intent = builder.buildSendIntent(Uri.parse("content://example/export.zip"))

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(AlertShareIntentBuilder.ZIP_MIME_TYPE, intent.type)
        assertNull(intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
        assertNull(intent.getStringArrayExtra(Intent.EXTRA_CC))
        assertNull(intent.getStringArrayExtra(Intent.EXTRA_BCC))
        assertFalse(intent.hasExtra("password"))
        assertFalse(intent.hasExtra("api_key"))
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }
}
