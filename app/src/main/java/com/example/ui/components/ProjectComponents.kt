package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: ProjectStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        ProjectStatus.AVAILABLE -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Default.CheckCircleOutline)
        ProjectStatus.REQUEST_PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.HourglassTop)
        ProjectStatus.APPROVED -> Triple(Color(0xFFD1FAE5), Color(0xFF047857), Icons.Default.CheckCircle)
        ProjectStatus.PROPOSAL_NEEDED -> Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), Icons.Default.Description)
        ProjectStatus.IN_PROGRESS -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.Engineering)
        ProjectStatus.NEEDS_REVISION -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.ReportProblem)
        ProjectStatus.COMPLETED -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Icons.Default.Verified)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RequestStatusBadge(
    status: RequestStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        RequestStatus.PENDING -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        RequestStatus.APPROVED -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
        RequestStatus.DECLINED -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SubmissionStatusBadge(
    status: SubmissionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        SubmissionStatus.UNDER_REVIEW -> Pair(Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        SubmissionStatus.APPROVED -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
        SubmissionStatus.NEEDS_REVISION -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun DomainChip(
    domain: ProjectDomain,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) PrimaryBlue else PrimaryBlueContainer.copy(alpha = 0.5f)
    val textColor = if (isSelected) Color.White else PrimaryBlueVariant

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.then(
            if (onClick != null) Modifier.clip(RoundedCornerShape(8.dp)) else Modifier
        ),
        onClick = { onClick?.invoke() }
    ) {
        Text(
            text = domain.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DifficultyBadge(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (difficulty) {
        DifficultyLevel.BEGINNER -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        DifficultyLevel.INTERMEDIATE -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        DifficultyLevel.ADVANCED -> Pair(Color(0xFFFCE7F3), Color(0xFFBE185D))
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = difficulty.label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun MatchScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        score >= 85 -> Pair(SecondaryGreenContainer, SecondaryGreenVariant)
        score >= 60 -> Pair(PrimaryBlueContainer, PrimaryBlueVariant)
        else -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$score% Match",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun ProjectProgressCard(
    progressPercentage: Int,
    status: ProjectStatus,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Project Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculated from approved milestones",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryGreen
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SecondaryGreen,
                trackColor = SecondaryGreenContainer.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Status",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(status = status)
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryBlueContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(text = actionButtonText, color = Color.White)
            }
        }
    }
}

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary,
    errorTextColor = Color(0xFFDC2626),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color(0xFFF1F5F9),
    errorContainerColor = Color.White,
    cursorColor = PrimaryBlue,
    errorCursorColor = Color(0xFFDC2626),
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = Color(0xFF94A3B8),
    disabledBorderColor = Color(0xFFE2E8F0),
    errorBorderColor = Color(0xFFDC2626),
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = TextSecondary,
    disabledLabelColor = TextTertiary,
    errorLabelColor = Color(0xFFDC2626),
    focusedPlaceholderColor = TextTertiary,
    unfocusedPlaceholderColor = TextTertiary,
    disabledPlaceholderColor = TextTertiary,
    errorPlaceholderColor = Color(0xFFDC2626),
    focusedSupportingTextColor = TextSecondary,
    unfocusedSupportingTextColor = TextSecondary,
    errorSupportingTextColor = Color(0xFFDC2626)
)


