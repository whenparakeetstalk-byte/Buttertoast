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
    audio: AudioSynthesizer,
    haptics: HapticManager,
    prefs: GamePreferences,
    onBack: () -> Unit,
    onNextLevel: (Level) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember(level, knifeSkin) {
        ToastEngine(level = level, knifeSkin = knifeSkin, audio = audio, haptics = haptics)
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

