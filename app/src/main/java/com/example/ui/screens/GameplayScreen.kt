package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioSynthesizer
import com.example.audio.HapticManager
import com.example.data.GamePreferences
import com.example.data.LevelRepository
import com.example.engine.ToastEngine
import com.example.model.GameResult
import com.example.model.KnifeSkin
import com.example.model.Level
import com.example.model.PowerUpType
import com.example.ui.components.GameHud
import com.example.ui.components.LevelCompleteModal
import com.example.ui.components.SettingsModal
import com.example.ui.components.ToastBoardCanvas
import kotlinx.coroutines.delay

@Composable
fun GameplayScreen(
    level: Level,
    knifeSkin: KnifeSkin,
    powerUpInventory: Map<String, Int>,
    unlockedUpgrades: Set<String> = emptySet(),
    audio: AudioSynthesizer,
    haptics: HapticManager,
    prefs: GamePreferences,
    onBack: () -> Unit,
    onNextLevel: (Level) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember(level, knifeSkin, unlockedUpgrades) {
        ToastEngine(
            level = level,
            knifeSkin = knifeSkin,
            audio = audio,
            haptics = haptics,
            unlockedUpgrades = unlockedUpgrades
        )
    }

    var resultModalData by remember { mutableStateOf<GameResult?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var tutorialDismissed by remember { mutableStateOf(false) }

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
                engine.resetLevel(tutorial)
            },
            onDismiss = { showSettings = false }
        )
    }

    // Physical BackHandler navigation support
    BackHandler {
        onBack()
    }

    // Frame update loop (60 FPS)
    LaunchedEffect(engine) {
        while (!engine.isFinished) {
            delay(16)
            engine.updateSimulation(0.016f)
            if (engine.isFinished && resultModalData == null) {
                val res = engine.finishGame()
                resultModalData = res
                prefs.saveLevelResult(
                    levelId = res.levelId,
                    stars = res.stars,
                    coverage = res.coverage,
                    score = res.finalScore,
                    coinsAwarded = res.coinsEarned
                )
            }
        }
    }

    Scaffold(
        topBar = {
            GameHud(
                engine = engine,
                powerUpInventory = powerUpInventory,
                onUsePowerUp = { powerUp ->
                    if (prefs.consumePowerUp(powerUp)) {
                        engine.activatePowerUp(powerUp)
                    }
                },
                onBack = onBack,
                onRestart = {
                    resultModalData = null
                    engine.resetLevel(level)
                },
                onFinishAndServe = {
                    if (!engine.isFinished) {
                        val res = engine.finishGame()
                        resultModalData = res
                        prefs.saveLevelResult(
                            levelId = res.levelId,
                            stars = res.stars,
                            coverage = res.coverage,
                            score = res.finalScore,
                            coinsAwarded = res.coinsEarned
                        )
                    }
                },
                onOpenSettings = {
                    showSettings = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ToastBoardCanvas(
                engine = engine,
                modifier = Modifier.fillMaxSize()
            )

            // Interactive Tutorial Overlay for Level 0
            if (level.id == 0 && !tutorialDismissed && resultModalData == null) {
                val tutorialStep = when {
                    engine.butterOnBlade < 0.08f && engine.currentCoverage < 0.12f -> 1
                    engine.currentCoverage < level.targetCoverage -> 2
                    else -> 3
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2E1C0C).copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                        .testTag("tutorial_step_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "STEP $tutorialStep OF 3",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = when (tutorialStep) {
                                        1 -> "Scoop the Butter"
                                        2 -> "Glide & Spread"
                                        else -> "Serve Your Toast!"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A)
                                )
                            }

                            IconButton(
                                onClick = { tutorialDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide tutorial",
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (tutorialStep) {
                                1 -> "Drag the butter knife over the golden butter pat in the center. Watch butter load onto your blade!"
                                2 -> "Glide the knife smoothly across the toast to lay down an even golden layer. Soft butter spreads effortlessly!"
                                else -> "40% coverage reached! You can keep spreading to earn 3 stars, or tap 'Serve Toast!' at the top to complete Academy!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFFBEB),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Sensory Guidance Card for Level 14 (Helen Keller Butters Bread)
            if (level.isHapticOnly && !tutorialDismissed && resultModalData == null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1B4B).copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .border(1.5.dp, Color(0xFF818CF8), RoundedCornerShape(20.dp))
                        .testTag("sensory_guidance_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF6366F1),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "TOUCH ONLY",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Feel With Your Hands",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0E7FF)
                                )
                            }

                            IconButton(
                                onClick = { tutorialDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide guidance",
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (level.isSmallBoard) {
                                "The Ultimate Challenge! You are buttering a round bagel on a tiny cutting board with no sight or sound. Feel the vibration clicks around the bagel edges, mind the center hole, and keep your knife from sliding off the tiny board!"
                            } else {
                                "Bread and butter are invisible & muted. Glide your knife across the board; feel the vibration texture when on the bread, and clicks when crossing edges or scooping butter!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE0E7FF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Bus Lap Guidance Card for Level 15 (Rosa Parks Butters Bread)
            if (level.isBusRide && !tutorialDismissed && resultModalData == null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A).copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                        .testTag("bus_lap_guidance_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "BUS COMMUTE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Front Seat Lap Balance",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBAE6FD)
                                )
                            }

                            IconButton(
                                onClick = { tutorialDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide guidance",
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You're in the front passenger seat! The bus jolts and shakes over bumps. Keep steady knife control and spread butter on the napkin without getting any butter on the trouser fabric!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF0F9FF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Banana Mayo Sandwich Guidance Card for Level 16
            if (level.hasBananas && !tutorialDismissed && resultModalData == null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1917).copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .border(1.5.dp, Color(0xFFFACC15), RoundedCornerShape(20.dp))
                        .testTag("banana_mayo_guidance_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEAB308),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "SOUTHERN COMFORT",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "George's Banana Mayo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF08A)
                                )
                            }

                            IconButton(
                                onClick = { tutorialDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide guidance",
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "A vintage Southern staple! Scoop the rich mayonnaise and glide smoothly over the soft white bread and across the banana slices for an authentic sandwich.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFEF9C3),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Level complete result dialog
            resultModalData?.let { result ->
                val nextLevel = LevelRepository.levels.firstOrNull { it.id == level.id + 1 }
                LevelCompleteModal(
                    result = result,
                    hasNextLevel = nextLevel != null,
                    onNextLevel = {
                        resultModalData = null
                        if (nextLevel != null) {
                            onNextLevel(nextLevel)
                        } else {
                            onBack()
                        }
                    },
                    onReplay = {
                        resultModalData = null
                        engine.resetLevel(level)
                    },
                    onDismiss = {
                        resultModalData = null
                        onBack()
                    }
                )
            }
        }
    }
}

