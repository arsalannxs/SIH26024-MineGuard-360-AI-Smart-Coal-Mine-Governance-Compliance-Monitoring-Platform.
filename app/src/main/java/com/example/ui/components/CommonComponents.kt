package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ComplianceStatusChip(status: ComplianceStatus) {
    val (bgColor, textColor) = when (status) {
        ComplianceStatus.COMPLIANT -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        ComplianceStatus.PENDING -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
        ComplianceStatus.UNDER_REVIEW -> Teal.copy(alpha = 0.15f) to Teal
        ComplianceStatus.ACTION_REQUIRED -> SafetyOrange.copy(alpha = 0.18f) to SafetyOrange
        ComplianceStatus.OVERDUE -> CriticalRed.copy(alpha = 0.18f) to CriticalRed
        ComplianceStatus.NOT_APPLICABLE -> Slate.copy(alpha = 0.15f) to Slate
    }
    StatusBadge(text = status.label, containerColor = bgColor, contentColor = textColor)
}

@Composable
fun SeverityChip(severity: IssueSeverity) {
    val (bgColor, textColor) = when (severity) {
        IssueSeverity.LOW -> Teal.copy(alpha = 0.15f) to Teal
        IssueSeverity.MEDIUM -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
        IssueSeverity.HIGH -> SafetyOrange.copy(alpha = 0.18f) to SafetyOrange
        IssueSeverity.CRITICAL -> CriticalRed.copy(alpha = 0.20f) to CriticalRed
    }
    StatusBadge(text = severity.label, containerColor = bgColor, contentColor = textColor)
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = Teal,
    cardBorderColor: Color = BorderColor,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorderColor))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MainText,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = DeepNavy)
                    Text("Role-Based Access Control", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    "Simulated demonstration switch. Test permissions across all 9 roles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = role == currentRole
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRoleSelected(role) }
                            .testTag("role_option_${role.name.lowercase()}"),
                        color = if (isSelected) LightTeal else CloudBackground,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Teal)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) DeepNavy else MainText,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = role.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryText
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("close_role_dialog")
            ) {
                Text("Apply Role")
            }
        }
    )
}
