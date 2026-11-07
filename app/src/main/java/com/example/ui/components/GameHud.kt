package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ToastEngine
import com.example.model.PowerUpType
import kotlin.math.roundToInt

@Composable
fun GameHud(
    engine: ToastEngine,
    powerUpInventory: Map<String, Int>,
    onUsePowerUp: (PowerUpType) -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onFinishAndServe: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val level = engine.level
    val coveragePercent = (engine.currentCoverage * 100).roundToInt().coerceIn(0, 100)
    val targetPercent = (level.targetCoverage * 100).roundToInt()
    val isTargetMet = engine.currentCoverage >= level.targetCoverage
    val tearsCount = engine.tearsCount
    val maxTears = level.maxTearsAllowed
    val tearsDanger = tearsCount >= maxTears
    val displayScore = engine.currentScore.roundToInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top row: Back button, Score Ticker (Starts at 1000), Restart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .testTag("hud_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Levels",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Central Score Board: Initial 1000 points
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2E1C0C),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$displayScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFFBEB)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x33FFFFFF), CircleShape)
                        .testTag("hud_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x33FFFFFF), CircleShape)
                        .testTag("hud_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Level",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Metrics card: Coverage progress bar, Evenness, Tears, Time
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                // Coverage row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Butter Coverage",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isTargetMet) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Target reached",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "$coveragePercent% / $targetPercent%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTargetMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Progress bar
                val progressFraction = (engine.currentCoverage / level.targetCoverage).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isTargetMet) Color(0xFF2E7D32) else Color(0xFFF59E0B),
                    trackColor = Color(0x339E9E9E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val evennessPct = (engine.currentEvenness * 100).roundToInt().coerceIn(0, 100)
                    Column {
                        Text(text = "Evenness", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$evennessPct%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Column {
                        Text(text = "Tears", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$tearsCount / $maxTears",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tearsDanger) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                            if (tearsDanger) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(text = "Blade Butter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val bladePct = (engine.butterOnBlade * 100).roundToInt().coerceIn(0, 100)
                        Text(
                            text = if (engine.butterOnBlade > 0.05f) "Ready ($bladePct%)" else "Empty",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (engine.butterOnBlade > 0.05f) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Time Left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val secs = engine.timeRemainingSeconds.roundToInt()
                        Text(
                            text = "${secs}s",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (secs <= 10) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Active Power-ups Bar
        if (engine.activePowerUps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((type, timeLeft) in engine.activePowerUps) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = type.iconColor.copy(alpha = 0.9f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${timeLeft.roundToInt()}s",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Finish & Serve Toast Button (appears when target is reached)
        AnimatedVisibility(visible = isTargetMet) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onFinishAndServe,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("serve_toast_button")
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Serve Toast! (Finish Level)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Bottom Power-up inventory tray: Quick tap to activate
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Power-Ups:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            for (type in PowerUpType.entries) {
                val count = powerUpInventory[type.id] ?: 0
                val isActive = engine.activePowerUps.containsKey(type)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (count > 0 && !isActive) type.iconColor.copy(alpha = 0.2f) else Color(0x1A000000),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = count > 0 && !isActive) {
                            onUsePowerUp(type)
                        }
                        .testTag("powerup_${type.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.displayName.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (count > 0) type.iconColor else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "x$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
