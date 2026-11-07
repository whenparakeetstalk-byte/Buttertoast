package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioSynthesizer
import com.example.audio.HapticManager
import com.example.data.GamePreferences
import com.example.data.LevelRepository
import com.example.model.BreadType
import com.example.model.KnifeSkin
import com.example.model.Level
import com.example.model.PowerUpType
import com.example.ui.screens.BakeryShopScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.ToastBarScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    data object LevelSelect : Screen
    data class Gameplay(val level: Level) : Screen
    data object BakeryShop : Screen
    data object ToastBarSandbox : Screen
}

class MainActivity : ComponentActivity() {
    private val audioSynthesizer by lazy { AudioSynthesizer() }
    private val hapticManager by lazy { HapticManager(this) }
    private val gamePreferences by lazy { GamePreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ButterToastApp(
                        prefs = gamePreferences,
                        audio = audioSynthesizer,
                        haptics = hapticManager
                    )
                }
            }
        }
    }
}

@Composable
fun ButterToastApp(
    prefs: GamePreferences,
    audio: AudioSynthesizer,
    haptics: HapticManager
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.LevelSelect) }

    val unlockedLevel by prefs.highestUnlockedLevel.collectAsStateWithLifecycle()
    val totalStars by prefs.totalStars.collectAsStateWithLifecycle()
    val coins by prefs.coins.collectAsStateWithLifecycle()
    val selectedKnifeId by prefs.selectedKnifeId.collectAsStateWithLifecycle()
    val selectedBreadId by prefs.selectedBreadId.collectAsStateWithLifecycle()
    val unlockedKnives by prefs.unlockedKnives.collectAsStateWithLifecycle()
    val unlockedBreads by prefs.unlockedBreads.collectAsStateWithLifecycle()
    val powerUpInventory by prefs.powerUpInventory.collectAsStateWithLifecycle()
    val soundEnabled by prefs.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by prefs.hapticsEnabled.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(soundEnabled) {
        audio.isEnabled = soundEnabled
    }
    androidx.compose.runtime.LaunchedEffect(hapticsEnabled) {
        haptics.isEnabled = hapticsEnabled
    }

    val currentKnife = remember(selectedKnifeId) {
        KnifeSkin.fromId(selectedKnifeId)
    }

    when (val screen = currentScreen) {
        is Screen.LevelSelect -> {
            LevelSelectScreen(
                prefs = prefs,
                unlockedLevel = unlockedLevel,
                totalStars = totalStars,
                coins = coins,
                onSelectLevel = { level ->
                    currentScreen = Screen.Gameplay(level)
                },
                onOpenShop = {
                    currentScreen = Screen.BakeryShop
                },
                onOpenSandbox = {
                    currentScreen = Screen.ToastBarSandbox
                }
            )
        }
        is Screen.Gameplay -> {
            GameplayScreen(
                level = screen.level,
                knifeSkin = currentKnife,
                powerUpInventory = powerUpInventory,
                audio = audio,
                haptics = haptics,
                prefs = prefs,
                onBack = {
                    currentScreen = Screen.LevelSelect
                },
                onNextLevel = { nextLevel ->
                    currentScreen = Screen.Gameplay(nextLevel)
                }
            )
        }
        is Screen.BakeryShop -> {
            BackHandler {
                currentScreen = Screen.LevelSelect
            }
            BakeryShopScreen(
                coins = coins,
                stars = totalStars,
                selectedKnifeId = selectedKnifeId,
                selectedBreadId = selectedBreadId,
                unlockedKnives = unlockedKnives,
                unlockedBreads = unlockedBreads,
                powerUpInventory = powerUpInventory,
                onSelectKnife = { prefs.setSelectedKnife(it) },
                onSelectBread = { prefs.setSelectedBread(it) },
                onUnlockKnife = { knife ->
                    prefs.unlockKnifeWithCoins(knife)
                },
                onUnlockBread = { bread ->
                    prefs.unlockBreadWithCoins(bread)
                },
                onBuyPowerUp = { powerUp ->
                    prefs.buyPowerUp(powerUp)
                },
                onBack = {
                    currentScreen = Screen.LevelSelect
                }
            )
        }
        is Screen.ToastBarSandbox -> {
            BackHandler {
                currentScreen = Screen.LevelSelect
            }
            ToastBarScreen(
                knifeSkin = currentKnife,
                unlockedBreads = unlockedBreads,
                audio = audio,
                haptics = haptics,
                onBack = {
                    currentScreen = Screen.LevelSelect
                }
            )
        }
    }
}
