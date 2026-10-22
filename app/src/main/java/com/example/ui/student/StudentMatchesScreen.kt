package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentMatchesScreen(
    user: UserAccount,
    recommendations: List<MatchRecommendation>,
    onSelectProblem: (ProjectProblem) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var minScoreFilter by remember { mutableFloatStateOf(50f) }

    val filteredRecommendations = remember(recommendations, minScoreFilter) {
        recommendations.filter { it.matchPercentage >= minScoreFilter.toInt() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudentMarbleBg)
    ) {
        // Algorithm Explanation Header Card
        Surface(
            color = SurfaceLight,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SecondaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Intelligent Match Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = StudentMarbleDark
                            )
                        }
                        Text(
                            text = "Deterministic multi-factor scoring comparing your verified skills & domain interests with faculty problem statements.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryGreenContainer)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit Profile Skills",
                            tint = SecondaryGreenVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Skills Snapshot
                Text(
                    text = "Your Profile Anchor Skills (${user.skills.size}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    user.skills.take(5).forEach { skill ->
                        Surface(
                            color = PrimaryBlueContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlueVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (user.skills.size > 5) {
                        Text(
                            text = "+${user.skills.size - 5} more",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score Threshold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minimum Match Score Filter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${minScoreFilter.toInt()}%+",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Slider(
                    value = minScoreFilter,
                    onValueChange = { minScoreFilter = it },
                    valueRange = 25f..90f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue,
                        inactiveTrackColor = PrimaryBlueContainer
                    )
                )
            }
        }

        // List of Matches
        if (filteredRecommendations.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FilterAltOff,
                title = "No Matches Above ${minScoreFilter.toInt()}%",
                message = "Lower the score slider or update your profile skills and domain interests to see more matching faculty projects.",
                actionButtonText = "Update Profile Skills",
                onActionClick = onNavigateToProfile
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecommendations) { rec ->
                    MatchRecommendationCard(
                        recommendation = rec,
                        userSkills = user.skills,
                        onClick = { onSelectProblem(rec.project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchRecommendationCard(
    recommendation: MatchRecommendation,
    userSkills: List<String>,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainChip(domain = recommendation.project.domain)
                MatchScoreBadge(score = recommendation.matchPercentage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recommendation.project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudentMarbleDark
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.project.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Explanation Box
            Surface(
                color = SecondaryGreenContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recommendation.explanation,
                        fontSize = 11.sp,
                        color = SecondaryGreenVariant,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skill Alignment Breakdown
            Text(
                text = "Skill Alignment Breakdown:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recommendation.project.requiredSkills.forEach { skill ->
                    val isMatched = recommendation.matchedSkills.any { it.equals(skill, ignoreCase = true) }
                    Surface(
                        color = if (isMatched) SecondaryGreenContainer else SurfaceVariantLight,
                        shape = RoundedCornerShape(6.dp),
                        border = if (isMatched) androidx.compose.foundation.BorderStroke(1.dp, SecondaryGreenLight) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isMatched) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isMatched) SecondaryGreenVariant else TextTertiary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = skill,
                                fontSize = 10.sp,
                                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMatched) SecondaryGreenVariant else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderStroke)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mentor: ${recommendation.project.staffName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${recommendation.project.staffDept} • Due: ${recommendation.project.deadline}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = StudentBlueAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Apply & Request", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
