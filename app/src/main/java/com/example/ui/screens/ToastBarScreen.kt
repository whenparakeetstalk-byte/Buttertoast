package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynthesizer
import com.example.audio.HapticManager
import com.example.engine.ButterPat
import com.example.engine.ToastEngine
import com.example.model.BreadType
import com.example.model.ButterType
import com.example.model.KnifeSkin
import com.example.model.Level
import com.example.ui.components.ToastBoardCanvas
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ToastBarScreen(
    knifeSkin: KnifeSkin,
    unlockedBreads: Set<String>,
    audio: AudioSynthesizer,
    haptics: HapticManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBread by remember { mutableStateOf(BreadType.WHITE) }
    var selectedButter by remember { mutableStateOf(ButterType.SALTED_CLASSIC) }
    var toastTemp by remember { mutableFloatStateOf(0.7f) }
    var butterHardness by remember { mutableFloatStateOf(0.35f) }

    val sandboxLevel = remember(selectedBread, selectedButter, toastTemp, butterHardness) {
        Level(
            id = 999,
            title = "Toast Bar Sandbox",
            subtitle = "Free buttering lab",
            hint = "Butter to your heart's content! Tap '+ Pat' for extra butter.",
            breadType = selectedBread,
            butterType = selectedButter,
            patCount = 1,
            butterHardness = butterHardness,
            toastTemp = toastTemp,
            targetCoverage = 0.80f,
            targetEvenness = 0.70f,
            maxTearsAllowed = 99,
            timeLimitSeconds = 9999
        )
    }

    val engine = remember(sandboxLevel, knifeSkin) {
        ToastEngine(level = sandboxLevel, knifeSkin = knifeSkin, audio = audio, haptics = haptics)
    }

    // Run simulation tick loop
    LaunchedEffect(engine) {
        while (true) {
            delay(16)
            engine.updateSimulation(0.016f)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("sandbox_back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "The Toast Bar (Sandbox)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Coverage: ${(engine.currentCoverage * 100).roundToInt()}% | Evenness: ${(engine.currentEvenness * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Add butter pat button
                        IconButton(
                            onClick = {
                                engine.butterPats.add(ButterPat(x = 0.5f, y = 0.5f, volume = 0.9f))
                                audio.playSliceSound()
                                haptics.vibrateSlice()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFD54F).copy(alpha = 0.4f), CircleShape)
                                .testTag("sandbox_add_pat_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Butter Pat", tint = Color(0xFFB45309))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Reset toast button
                        IconButton(
                            onClick = { engine.resetLevel(sandboxLevel) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x22000000), CircleShape)
                                .testTag("sandbox_reset_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Toast")
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Select Bread Flavor
                    Text(text = "Select Toast:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BreadType.entries) { bread ->
                            val isUnlocked = unlockedBreads.contains(bread.id)
                            val isSelected = selectedBread == bread

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFD97706) else Color(0xFFE2C499).copy(alpha = 0.3f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = isUnlocked) {
                                        selectedBread = bread
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(bread.crustColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = bread.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Select Butter Flavor
                    Text(text = "Select Butter:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ButterType.entries) { butter ->
                            val isSelected = selectedButter == butter

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFF59E0B) else Color(0xFFFFD54F).copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedButter = butter }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(butter.solidColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = butter.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Heat & Hardness Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Toast Heat: ${(toastTemp * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Slider(
                                value = toastTemp,
                                onValueChange = { toastTemp = it },
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFD97706), activeTrackColor = Color(0xFFD97706))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Butter Hardness: ${(butterHardness * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Slider(
                                value = butterHardness,
                                onValueChange = { butterHardness = it },
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                            )
                        }
                    }
                }
            }
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
        }
    }
}
