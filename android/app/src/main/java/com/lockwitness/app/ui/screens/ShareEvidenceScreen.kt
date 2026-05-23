package com.lockwitness.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lockwitness.app.ui.components.ForensicCard
import com.lockwitness.app.ui.components.ForensicDivider
import com.lockwitness.app.ui.components.SectionEyebrow
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.GraphiteBg
import com.lockwitness.app.ui.theme.HashText
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen
import java.io.File

@Composable
fun ShareEvidenceScreen(
    filePath: String,
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }
    val fileSizeMb = remember(file) {
        if (file.exists()) {
            val bytes = file.length()
            if (bytes < 1024 * 1024) "${bytes / 1024} KB" else "${"%.1f".format(bytes / 1024.0 / 1024.0)} MB"
        } else "File not found"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .padding(contentPadding)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Share Evidence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Package summary
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                        SectionEyebrow("Evidence Package")
                    }
                    ForensicDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("File", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(file.name, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = HashText, maxLines = 1)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Size", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(fileSizeMb, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Format", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("ZIP archive", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(if (file.exists()) "Ready" else "Missing", style = MaterialTheme.typography.bodySmall, color = if (file.exists()) VerifiedGreen else CautionAmber)
                    }
                }
            }

            // Privacy warning
            ForensicCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = CautionAmber, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Privacy Notice", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "This package may contain photos, video, and GPS location data. Share only with trusted parties — law enforcement, legal counsel, or personal backup storage. Once shared, you cannot control who accesses it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Share button
            Button(
                onClick = {
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Evidence Package"))
                    }
                },
                enabled = file.exists(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Share via Android", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, StrokeSubtle)
            ) {
                Text("Done", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
