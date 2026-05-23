package com.lockwitness.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lockwitness.app.ui.theme.CardSurface
import com.lockwitness.app.ui.theme.CautionAmber
import com.lockwitness.app.ui.theme.DestructiveRed
import com.lockwitness.app.ui.theme.HashText
import com.lockwitness.app.ui.theme.MutedChip
import com.lockwitness.app.ui.theme.ProOrange
import com.lockwitness.app.ui.theme.StrokeSubtle
import com.lockwitness.app.ui.theme.TextPrimary
import com.lockwitness.app.ui.theme.TextSecondary
import com.lockwitness.app.ui.theme.VerifiedGreen

@Composable
fun ForensicCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, StrokeSubtle)
    ) {
        content()
    }
}

enum class PillType { ARMED, PAUSED, PASS, FAIL, UNAVAILABLE, VERIFIED, HASHED, UNEXPORTED, PRO, BEST_VALUE, FOUNDER, DELETE }

fun pillColor(type: PillType): Color = when (type) {
    PillType.ARMED, PillType.PASS, PillType.VERIFIED, PillType.HASHED -> VerifiedGreen
    PillType.PAUSED, PillType.UNAVAILABLE, PillType.UNEXPORTED -> CautionAmber
    PillType.PRO, PillType.BEST_VALUE, PillType.FOUNDER -> ProOrange
    PillType.FAIL, PillType.DELETE -> DestructiveRed
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
    color: Color = HashText
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.50f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun SpecPill(type: PillType, modifier: Modifier = Modifier) {
    val color = pillColor(type)
    StatusPill(text = type.name.replace('_', ' '), color = color, modifier = modifier)
}

@Composable
fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = modifier
    )
}

@Composable
fun MutedText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        modifier = modifier
    )
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    detail: String,
    pillText: String,
    pillColor: Color,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForensicCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = pillColor, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    StatusPill(text = pillText, color = pillColor)
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            OutlinedButton(
                onClick = onAction,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = pillColor),
                border = BorderStroke(1.dp, pillColor.copy(alpha = 0.6f))
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ForensicDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = StrokeSubtle)
}
