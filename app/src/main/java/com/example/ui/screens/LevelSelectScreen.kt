package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.model.ButterType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SettingsModal
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferences
import com.example.data.LevelRepository
import com.example.model.Level

@Composable
fun LevelSelectScreen(
    prefs: GamePreferences,
    unlockedLevel: Int,
    totalStars: Int,
    coins: Int,
    spendablePoints: Int,
    onSelectLevel: (Level) -> Unit,
    onOpenShop: (Int) -> Unit,
    onOpenSandbox: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    val soundEnabled by prefs.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by prefs.hapticsEnabled.collectAsStateWithLifecycle()

    if (showSettings) {
        SettingsModal(
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            onToggleSound = { prefs.setSoundEnabled(it) },
            onToggleHaptics = { prefs.setHapticsEnabled(it) },
            onStartTutorial = {
                val tutorial = LevelRepository.levels.firstOrNull { it.id == 0 } ?: LevelRepository.levels.first()
                onSelectLevel(tutorial)
            },
            onDismiss = { showSettings = false }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Header title & currencies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BUTTER TOAST",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Master the art of the butter knife",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Currency badges: Coins, Points & Stars (clickable to open Point Perks / Shop)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenShop(0) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "$coins", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB45309))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEDE9FE),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenShop(3) } // Opens Point Perks tab!
                                    .testTag("spendable_points_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "$spendablePoints pts", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF6B21A8))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFE082).copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "$totalStars", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF78350F))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top Navigation Bar: Levels (Current), Bakery (Breads/Knives), Bakery Shop (Points & Power-Ups)
                    TabRow(
                        selectedTabIndex = 0,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = true,
                            onClick = { /* already on levels */ },
                            text = {
                                Text(
                                    text = "Levels",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = false,
                            onClick = { onOpenShop(0) }, // Tab 0: Butter Knives & Breads
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFB45309))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bakery",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_bakery")
                        )
                        Tab(
                            selected = false,
                            onClick = { onOpenShop(3) }, // Tab 3: Point Perks & Upgrades
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF7E22CE))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bakery Shop",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_bakery_shop")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Action buttons: Bakery, Bakery Shop (Spend Points), and Toast Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenShop(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("open_bakery_button")
                        ) {
                            Text("Bakery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onOpenShop(3) }, // Direct to spend points
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.35f)
                                .height(40.dp)
                                .testTag("open_shop_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bakery Shop (Perks)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenSandbox,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.05f)
                                .height(40.dp)
                                .testTag("open_sandbox_button")
                        ) {
                            Text("Toast Bar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Options",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(LevelRepository.levels) { level ->
                val isUnlocked = level.id == 0 || level.id <= unlockedLevel
                val stars = prefs.getStarsForLevel(level.id)
                val bestScore = prefs.getBestScoreForLevel(level.id)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(enabled = isUnlocked) { onSelectLevel(level) }
                        .border(
                            width = 1.dp,
                            color = if (isUnlocked) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.LightGray,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("level_item_${level.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Level number badge
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        when {
                                            !isUnlocked -> Brush.linearGradient(listOf(Color(0xFF9E9E9E), Color(0xFF757575)))
                                            level.isHapticOnly -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF312E81)))
                                            level.isBusRide -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
                                            else -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    if (level.isBusRide) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = "Bus Commute",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else if (level.isHapticOnly) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = "Haptic Only",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else if (level.id == 0) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = "Tutorial",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${level.id}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = level.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF757575)
                                    )
                                    if (level.id == 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "TUTORIAL",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0284C7),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.isCeramicPlate) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF0FDF4),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "CERAMIC PLATE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.isSmallBoard) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEE2E2),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "TINY BOARD",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFDC2626),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.isHapticOnly) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEDE9FE),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "HAPTIC BLIND",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF6D28D9),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.isBusRide) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "BUS COMMUTE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.hasBananas) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF9C3),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "BANANA MAYO",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF854D0E),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (level.butterType == ButterType.AVOCADO_CREAM) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7),
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = "AVOCADO",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${level.breadType.displayName} • ${level.butterType.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray
                                )

                                if (isUnlocked && bestScore > 0) {
                                    Text(
                                        text = "Best: $bestScore pts",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        // Right side: Stars earned or Locked label
                        if (isUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (i <= stars) Color(0xFFFFB300) else Color(0x339E9E9E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color(0xFFD97706)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x1F000000)
                            ) {
                                Text(
                                    text = "Locked",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
