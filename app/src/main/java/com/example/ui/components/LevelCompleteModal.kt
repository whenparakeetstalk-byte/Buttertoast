package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameResult
import kotlin.math.roundToInt

@Composable
fun LevelCompleteModal(
    result: GameResult,
    hasNextLevel: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onDismiss: () -> Unit
) {
    val starScale1 = remember { Animatable(0f) }
    val starScale2 = remember { Animatable(0f) }
    val starScale3 = remember { Animatable(0f) }

    LaunchedEffect(result.stars) {
        if (result.stars >= 1) {
            starScale1.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale1.animateTo(1.0f)
        }
        if (result.stars >= 2) {
            starScale2.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale2.animateTo(1.0f)
        }
        if (result.stars >= 3) {
            starScale3.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale3.animateTo(1.0f)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (result.passed) Color(0xFFD97706) else Color(0xFFD32F2F), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title
                Text(
                    text = if (result.passed) "LEVEL COMPLETE!" else "LEVEL FAILED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (result.passed) Color(0xFFD97706) else Color(0xFFD32F2F),
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.ratingTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Golden Stars Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val starModifier = Modifier.size(44.dp)
                    // Star 1
                    Box(modifier = Modifier.scale(if (result.stars >= 1) starScale1.value else 1f)) {
                        Icon(
                            imageVector = if (result.stars >= 1) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (result.stars >= 1) Color(0xFFFFB300) else Color(0x449E9E9E),
                            modifier = starModifier
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Star 2
                    Box(modifier = Modifier.scale(if (result.stars >= 2) starScale2.value else 1f)) {
                        Icon(
                            imageVector = if (result.stars >= 2) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (result.stars >= 2) Color(0xFFFFB300) else Color(0x449E9E9E),
                            modifier = starModifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Star 3
                    Box(modifier = Modifier.scale(if (result.stars >= 3) starScale3.value else 1f)) {
                        Icon(
                            imageVector = if (result.stars >= 3) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (result.stars >= 3) Color(0xFFFFB300) else Color(0x449E9E9E),
                            modifier = starModifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score Breakdown Surface
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFFBEB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Score:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF78350F))
                            Text("+${result.initialScore}", fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                        }

                        if (result.perfectionBonus > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Perfection Bonus:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                Text("+${result.perfectionBonus}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        if (result.timeScoreLoss > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Decay:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                                Text("-${result.timeScoreLoss}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }

                        if (result.boundaryPenalties > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Boundary Spills:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                                Text("-${result.boundaryPenalties}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }

                        if (result.tearPenalties > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Torn Holes:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                                Text("-${result.tearPenalties}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2C499)))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FINAL SCORE:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF451A03))
                            Text("${result.finalScore}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFFD97706))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coins & Points earned banners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${result.coinsEarned} Coins",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${result.finalScore} Pts",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B21A8),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (result.levelId == 14) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF312E81),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ Sensory Challenge Conquered!\nThe blindfold is lifted - you buttered this purely through touch!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE0E7FF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else if (result.levelId == 15) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0369A1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🚌 Commute Complete!\nYou navigated every bump and buttered your toast on your lap without getting any butter on your clothes!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F9FF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.feedbackMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Next Level, Retry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("dialog_replay_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry")
                    }

                    if (result.passed && hasNextLevel) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("dialog_next_level_button")
                        ) {
                            Text("Next Level", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
