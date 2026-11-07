package com.example.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.AudioSynthesizer
import com.example.audio.HapticManager
import com.example.model.BreadType
import com.example.model.ButterType
import com.example.model.GameResult
import com.example.model.KnifeSkin
import com.example.model.Level
import com.example.model.PowerUpToken
import com.example.model.PowerUpType
import com.example.model.ScoreFloatingPopup
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class ButterPat(
    var x: Float, // Normalized 0..1 in toast space
    var y: Float,
    var volume: Float, // Starts at 1.0f
    val initialSize: Float = 0.09f,
    var isCut: Boolean = false,
    var lastSliceTime: Long = 0L
)

data class GridCell(
    var butter: Float = 0f,       // 0.0 to 2.0+
    var melt: Float = 0f,         // 0.0 (solid) to 1.0 (melted)
    var isTorn: Boolean = false,
    val crumbNoise: Float = Random.nextFloat(),
    var flecks: Boolean = false
)

data class ToastParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color,
    val isSteam: Boolean = false,
    val isGlow: Boolean = false
)

class ToastEngine(
    var level: Level,
    var knifeSkin: KnifeSkin = KnifeSkin.CLASSIC_SILVER,
    private val audio: AudioSynthesizer,
    private val haptics: HapticManager
) {
    val gridWidth = 44
    val gridHeight = 44
    val grid: Array<Array<GridCell>> = Array(gridWidth) {
        Array(gridHeight) { GridCell() }
    }

    val breadMask: Array<BooleanArray> = Array(gridWidth) { BooleanArray(gridHeight) }
    val butterPats = mutableListOf<ButterPat>()
    val particles = mutableListOf<ToastParticle>()
    val powerUpTokens = mutableListOf<PowerUpToken>()
    val scorePopups = mutableListOf<ScoreFloatingPopup>()

    // Knife state (Reactive Compose States)
    var knifePos by mutableStateOf(Offset(0.85f, 0.85f))
    var knifeAngle by mutableFloatStateOf(-35f) // Degrees: steady ergonomic hold
    var butterOnBlade by mutableFloatStateOf(0.0f) // Starts clean, pickup from pat!
    var isDragging by mutableStateOf(false)
    var redrawTick by mutableLongStateOf(0L)
    private var lastDragPos: Offset? = null
    private var lastSoundTime: Long = 0
    private var lastTearTime: Long = 0
    private var popupIdCounter: Long = 0

    // Scoring system (Starts with 1000 points!)
    val initialScore: Int = 1000
    var currentScore by mutableFloatStateOf(1000f)
    var timeScoreLoss by mutableFloatStateOf(0f)
    var boundaryPenaltiesTotal by mutableIntStateOf(0)
    var tearPenaltiesTotal by mutableIntStateOf(0)
    var dropPenaltiesTotal by mutableIntStateOf(0)
    var perfectionBonusTotal by mutableIntStateOf(0)

    // Power-up active states & timers
    val activePowerUps = mutableMapOf<PowerUpType, Float>() // Type -> seconds remaining
    var knifeWidthMultiplier by mutableFloatStateOf(1.0f)
    var noMessShieldActive by mutableStateOf(false)
    var autoSpreadActive by mutableStateOf(false)
    var goldenMeltActive by mutableStateOf(false)
    var infiniteButterActive by mutableStateOf(false)
    var butterMagnetActive by mutableStateOf(false)

    // Auto-spread helper
    private var autoSpreadAngle: Float = 0f

    // Score & statistics
    var currentCoverage by mutableFloatStateOf(0f)
    var currentEvenness by mutableFloatStateOf(0f)
    var tearsCount by mutableIntStateOf(0)
    var timeRemainingSeconds by mutableFloatStateOf(level.timeLimitSeconds.toFloat())
    var isFinished by mutableStateOf(false)
    private var tokenSpawnTimer: Float = 0f

    init {
        resetLevel(level)
    }

    fun resetLevel(newLevel: Level) {
        level = newLevel
        timeRemainingSeconds = newLevel.timeLimitSeconds.toFloat()
        isFinished = false
        tearsCount = 0
        butterOnBlade = 0.0f
        knifeAngle = -35f
        lastDragPos = null
        particles.clear()
        powerUpTokens.clear()
        scorePopups.clear()
        activePowerUps.clear()
        tokenSpawnTimer = 3f

        // Reset scoring to 1000
        currentScore = 1000f
        timeScoreLoss = 0f
        boundaryPenaltiesTotal = 0
        tearPenaltiesTotal = 0
        dropPenaltiesTotal = 0
        perfectionBonusTotal = 0

        resetPowerUpMultipliers()

        // Initialize grid cells
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                grid[x][y].butter = 0f
                grid[x][y].melt = 0f
                grid[x][y].isTorn = false
                grid[x][y].flecks = false
            }
        }

        // Compute bread shape mask
        for (x in 0 until gridWidth) {
            val nx = x.toFloat() / (gridWidth - 1)
            for (y in 0 until gridHeight) {
                val ny = y.toFloat() / (gridHeight - 1)
                breadMask[x][y] = checkInsideBread(nx, ny, level.breadType)
            }
        }

        // Spawn butter pats according to level (generous 30-35% higher volume)
        butterPats.clear()
        when (level.patCount) {
            1 -> {
                butterPats.add(ButterPat(x = 0.5f, y = 0.48f, volume = 1.35f))
            }
            2 -> {
                if (level.breadType == BreadType.BAGEL) {
                    butterPats.add(ButterPat(x = 0.28f, y = 0.5f, volume = 1.30f))
                    butterPats.add(ButterPat(x = 0.72f, y = 0.5f, volume = 1.30f))
                } else {
                    butterPats.add(ButterPat(x = 0.35f, y = 0.45f, volume = 1.15f))
                    butterPats.add(ButterPat(x = 0.65f, y = 0.52f, volume = 1.15f))
                }
            }
            else -> {
                butterPats.add(ButterPat(x = 0.30f, y = 0.35f, volume = 1.0f))
                butterPats.add(ButterPat(x = 0.70f, y = 0.38f, volume = 1.0f))
                butterPats.add(ButterPat(x = 0.50f, y = 0.62f, volume = 1.0f))
            }
        }

        // Spawn 1 initial floating power-up token on the board
        spawnRandomPowerUpToken()

        // Start knife position comfortably beside the toast
        knifePos = Offset(0.85f, 0.85f)
        knifeAngle = -35f
        recomputeMetrics()
    }

    private fun resetPowerUpMultipliers() {
        knifeWidthMultiplier = 1.0f
        noMessShieldActive = false
        autoSpreadActive = false
        goldenMeltActive = false
        infiniteButterActive = false
        butterMagnetActive = false
    }

    fun activatePowerUp(type: PowerUpType) {
        activePowerUps[type] = type.durationSeconds
        audio.playStarSound(2)
        haptics.vibrateSuccess()
        spawnFloatingScorePopup("+ ${type.displayName}", knifePos.x, knifePos.y, isPositive = true)

        when (type) {
            PowerUpType.WIDE_KNIFE -> knifeWidthMultiplier = 1.8f
            PowerUpType.NO_MESS_SHIELD -> noMessShieldActive = true
            PowerUpType.AUTO_SPREAD -> {
                autoSpreadActive = true
                butterOnBlade = 1.0f
            }
            PowerUpType.INFINITE_BUTTER -> {
                infiniteButterActive = true
                butterOnBlade = 1.0f
            }
            PowerUpType.GOLDEN_MELT -> {
                goldenMeltActive = true
                // Immediately melt surface butter
                for (x in 0 until gridWidth) {
                    for (y in 0 until gridHeight) {
                        if (breadMask[x][y] && grid[x][y].butter > 0.05f) {
                            grid[x][y].melt = (grid[x][y].melt + 0.5f).coerceAtMost(1.0f)
                        }
                    }
                }
            }
            PowerUpType.BUTTER_MAGNET -> {
                butterMagnetActive = true
            }
        }
    }

    private fun spawnRandomPowerUpToken() {
        if (powerUpTokens.size >= 2) return
        val types = PowerUpType.entries
        val type = types.random()
        // Spawn near the edge of the toast or on the board
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val dist = 0.35f + Random.nextFloat() * 0.12f
        val x = (0.5f + cos(angle) * dist).coerceIn(0.15f, 0.85f)
        val y = (0.5f + sin(angle) * dist).coerceIn(0.15f, 0.85f)

        powerUpTokens.add(
            PowerUpToken(
                id = ++popupIdCounter,
                type = type,
                x = x,
                y = y
            )
        )
    }

    private fun checkInsideBread(nx: Float, ny: Float, type: BreadType): Boolean {
        val cx = nx - 0.5f
        val cy = ny - 0.5f

        return when (type) {
            BreadType.BAGEL -> {
                val dist = sqrt(cx * cx + cy * cy)
                dist in 0.10f..0.45f
            }
            BreadType.SOURDOUGH -> {
                val dx = cx / 0.44f
                val dy = cy / 0.38f
                (dx * dx + dy * dy) <= 1.0f
            }
            BreadType.BRIOCHE -> {
                if (cx in -0.42f..0.42f && cy in -0.2f..0.42f) {
                    true
                } else if (cy < -0.2f && cy > -0.44f && cx in -0.42f..0.42f) {
                    val archOffset = sin(cx * 15f) * 0.05f
                    cy > -0.42f + archOffset
                } else {
                    false
                }
            }
            BreadType.WAFFLE -> {
                val inBounds = cx in -0.42f..0.42f && cy in -0.42f..0.42f
                if (!inBounds) return false
                val cornerDist = hypot(
                    (kotlin.math.abs(cx) - 0.36f).coerceAtLeast(0f),
                    (kotlin.math.abs(cy) - 0.36f).coerceAtLeast(0f)
                )
                cornerDist < 0.06f
            }
            BreadType.RYE -> {
                val inBounds = cx in -0.45f..0.45f && cy in -0.38f..0.38f
                if (!inBounds) return false
                val corner = hypot(
                    (kotlin.math.abs(cx) - 0.35f).coerceAtLeast(0f),
                    (kotlin.math.abs(cy) - 0.28f).coerceAtLeast(0f)
                )
                corner < 0.10f
            }
            BreadType.WHITE -> {
                if (cx !in -0.42f..0.42f) return false
                if (cy in -0.15f..0.42f) return true
                if (cy < -0.15f && cy >= -0.45f) {
                    val topY = (cy + 0.15f) / 0.30f
                    val topX = cx / 0.43f
                    (topX * topX + topY * topY) <= 1.0f
                } else {
                    false
                }
            }
        }
    }

    fun onDragStart(pos: Offset) {
        isDragging = true
        lastDragPos = pos
        knifePos = pos
        redrawTick++
    }

    fun onDrag(newPos: Offset) {
        val prev = lastDragPos ?: newPos
        val dx = newPos.x - prev.x
        val dy = newPos.y - prev.y
        val dist = hypot(dx, dy)

        knifePos = newPos

        // Steady ergonomic knife holding angle (-35°): completely stable, never spins or flips
        knifeAngle = -35f

        // Check interaction with Power-Up Tokens
        val tokenIter = powerUpTokens.iterator()
        while (tokenIter.hasNext()) {
            val token = tokenIter.next()
            val dToken = hypot(newPos.x - token.x, newPos.y - token.y)
            val collectRadius = if (butterMagnetActive) 0.16f else 0.08f
            if (dToken < collectRadius) {
                tokenIter.remove()
                activatePowerUp(token.type)
            }
        }

        // Check interaction with Butter Pats (controlled realistic slice with cooldown)
        val nowSlice = System.currentTimeMillis()
        for (pat in butterPats) {
            if (pat.volume > 0.05f) {
                val dPat = hypot(newPos.x - pat.x, newPos.y - pat.y)
                val hitRadius = pat.initialSize * (if (butterMagnetActive) 1.8f else 1.0f)
                if (dPat < hitRadius && nowSlice - pat.lastSliceTime > 300L) {
                    pat.lastSliceTime = nowSlice
                    // Scoop clean, generous slice onto knife edge (caps at 0.85f)
                    butterOnBlade = (butterOnBlade + 0.28f).coerceAtMost(0.85f)
                    pat.volume = (pat.volume - 0.08f).coerceAtLeast(0.05f)

                    val patGx = (pat.x * gridWidth).toInt().coerceIn(0, gridWidth - 1)
                    val patGy = (pat.y * gridHeight).toInt().coerceIn(0, gridHeight - 1)
                    if (breadMask[patGx][patGy]) {
                        grid[patGx][patGy].butter = (grid[patGx][patGy].butter + 0.20f).coerceAtMost(1.2f)
                    }

                    audio.playSliceSound()
                    haptics.vibrateSlice()
                    spawnButterSparks(pat.x, pat.y, 4, level.butterType.solidColor)
                }
            }
        }

        // Blade sweep across toast cells with knife physical stats & multipliers (Wide knife scales both width & length!)
        val bladeRad = (knifeAngle + 90f) * PI.toFloat() / 180f
        val widthMult = knifeWidthMultiplier
        val lengthMult = 1.0f + (widthMult - 1.0f) * 0.65f
        val bladeLength = (knifeSkin.bladeLength / 1000f) * 0.55f * lengthMult
        val tipX = newPos.x + cos(bladeRad) * bladeLength * 0.5f
        val tipY = newPos.y + sin(bladeRad) * bladeLength * 0.5f
        val heelX = newPos.x - cos(bladeRad) * bladeLength * 0.5f
        val heelY = newPos.y - sin(bladeRad) * bladeLength * 0.5f

        val samples = (6 * widthMult).roundToInt().coerceIn(5, 14)
        var spreadHappened = false
        var scrapeSoundTriggered = false
        var boundarySpillDetected = false

        // Deposit butter with layer-aware efficiency so butter carries onto bare bread
        if (butterOnBlade > 0.01f || infiniteButterActive) {
            if (!infiniteButterActive) {
                val stepDepletion = (0.0012f + dist * 0.016f).coerceAtMost(butterOnBlade)
                butterOnBlade = (butterOnBlade - stepDepletion).coerceAtLeast(0f)
            } else {
                butterOnBlade = 1.0f
            }

            for (s in 0..samples) {
                val t = s.toFloat() / samples
                val sampleX = tipX * (1f - t) + heelX * t
                val sampleY = tipY * (1f - t) + heelY * t

                val gx = (sampleX * gridWidth).toInt()
                val gy = (sampleY * gridHeight).toInt()

                // Precise contact line deposit with layer awareness
                if (gx in 0 until gridWidth && gy in 0 until gridHeight) {
                    if (breadMask[gx][gy]) {
                        val cell = grid[gx][gy]
                        // If cell already has butter, absorb less so butter continues onto bare bread
                        val cellNeed = (0.80f - cell.butter).coerceIn(0.20f, 1.0f)
                        val depositWeight = 0.20f * cellNeed * (if (goldenMeltActive) 1.35f else 1.0f)
                        cell.butter = (cell.butter + depositWeight).coerceAtMost(1.5f)
                        spreadHappened = true

                        // Butter Magnet draws butter to adjacent empty cells
                        if (butterMagnetActive) {
                            for (ox in -1..1) {
                                for (oy in -1..1) {
                                    val mx = gx + ox
                                    val my = gy + oy
                                    if (mx in 0 until gridWidth && my in 0 until gridHeight && breadMask[mx][my]) {
                                        val mCell = grid[mx][my]
                                        if (mCell.butter < 0.35f) {
                                            mCell.butter = (mCell.butter + 0.12f).coerceAtMost(1.2f)
                                        }
                                    }
                                }
                            }
                        }

                        if (level.butterType.hasFlecks && Random.nextFloat() < 0.10f) {
                            cell.flecks = true
                        }
                    }
                }
            }
        } else {
            // Empty blade pushes & redistributes existing butter pools to bare areas
            for (s in 0..samples) {
                val t = s.toFloat() / samples
                val sampleX = tipX * (1f - t) + heelX * t
                val sampleY = tipY * (1f - t) + heelY * t
                val gx = (sampleX * gridWidth).toInt()
                val gy = (sampleY * gridHeight).toInt()

                if (gx in 0 until gridWidth && gy in 0 until gridHeight && breadMask[gx][gy]) {
                    val cell = grid[gx][gy]
                    if (cell.butter > 0.24f) {
                        spreadHappened = true
                        val excess = (cell.butter - 0.24f) * 0.28f
                        cell.butter -= excess
                        val nx = (gx + (dx * 14f).toInt()).coerceIn(0, gridWidth - 1)
                        val ny = (gy + (dy * 14f).toInt()).coerceIn(0, gridHeight - 1)
                        if (breadMask[nx][ny]) {
                            grid[nx][ny].butter = (grid[nx][ny].butter + excess).coerceAtMost(1.5f)
                        }
                    } else if (dist > 0.02f) {
                        scrapeSoundTriggered = true
                    }
                }
            }
        }

        // Boundary spill check: only when the knife contact point is dragged off the bread
        val centerGx = (newPos.x * gridWidth).toInt().coerceIn(0, gridWidth - 1)
        val centerGy = (newPos.y * gridHeight).toInt().coerceIn(0, gridHeight - 1)
        if (!breadMask[centerGx][centerGy] && butterOnBlade > 0.05f && dist > 0.035f) {
            boundarySpillDetected = true
        }

        // Bread tearing check: ONLY on hard butter + fragile bread + aggressive fast dragging
        val nowTear = System.currentTimeMillis()
        val tearRisk = (level.butterHardness * 0.7f + level.breadType.fragility * 0.5f) / knifeSkin.controlFactor
        if (!noMessShieldActive && level.butterHardness > 0.60f && level.breadType.fragility > 0.50f && dist > 0.085f && tearRisk > 0.65f && nowTear - lastTearTime > 800) {
            val midGx = (newPos.x * gridWidth).toInt().coerceIn(0, gridWidth - 1)
            val midGy = (newPos.y * gridHeight).toInt().coerceIn(0, gridHeight - 1)
            if (breadMask[midGx][midGy] && !grid[midGx][midGy].isTorn) {
                grid[midGx][midGy].isTorn = true
                tearsCount++
                lastTearTime = nowTear
                deductScore(50, "Torn Bread!", newPos.x, newPos.y)
                tearPenaltiesTotal += 50
                audio.playTearSound()
                haptics.vibrateTear()
                spawnButterSparks(newPos.x, newPos.y, 6, level.breadType.crustColor)
            }
        }

        // Boundary spill deduction (unless shield is active)
        if (boundarySpillDetected && !noMessShieldActive) {
            val now = System.currentTimeMillis()
            if (now - lastSoundTime > 250) {
                deductScore(10, "Off Boundary!", newPos.x, newPos.y)
                boundaryPenaltiesTotal += 10
                haptics.vibrateTear()
                audio.playScrapeSound()
                lastSoundTime = now
            }
        }

        val now = System.currentTimeMillis()
        if (spreadHappened && now - lastSoundTime > 85) {
            audio.playSpreadSound(intensity = (dist * 15f).coerceIn(0.3f, 1f))
            haptics.vibrateSpread()
            lastSoundTime = now
        } else if (scrapeSoundTriggered && now - lastSoundTime > 150) {
            audio.playScrapeSound()
            lastSoundTime = now
        }

        lastDragPos = newPos
        recomputeMetrics()
        redrawTick++
    }

    fun onDragEnd() {
        // Check for sudden drop / release while knife is heavily loaded outside
        if (isDragging && butterOnBlade > 0.5f) {
            val gx = (knifePos.x * gridWidth).toInt()
            val gy = (knifePos.y * gridHeight).toInt()
            val inside = gx in 0 until gridWidth && gy in 0 until gridHeight && breadMask[gx][gy]
            if (!inside && !noMessShieldActive) {
                deductScore(25, "Dropped Knife!", knifePos.x, knifePos.y)
                dropPenaltiesTotal += 25
                haptics.vibrateTear()
            }
        }

        isDragging = false
        lastDragPos = null
        redrawTick++
    }

    private fun deductScore(points: Int, reason: String, x: Float, y: Float) {
        currentScore = (currentScore - points).coerceAtLeast(0f)
        spawnFloatingScorePopup("- $points $reason", x, y, isPositive = false)
    }

    private fun spawnFloatingScorePopup(text: String, x: Float, y: Float, isPositive: Boolean) {
        scorePopups.add(
            ScoreFloatingPopup(
                id = ++popupIdCounter,
                text = text,
                x = x.coerceIn(0.1f, 0.9f),
                y = y.coerceIn(0.1f, 0.9f),
                isPositive = isPositive
            )
        )
    }

    fun updateSimulation(dtSeconds: Float) {
        if (isFinished) return

        // Time score decay: decrease score slightly over time (2.5 pts/sec) to encourage speed
        val scoreDecay = dtSeconds * 2.5f
        currentScore = (currentScore - scoreDecay).coerceAtLeast(0f)
        timeScoreLoss += scoreDecay

        // Decrement timer
        timeRemainingSeconds = (timeRemainingSeconds - dtSeconds).coerceAtLeast(0f)
        if (timeRemainingSeconds <= 0f) {
            finishGame()
            return
        }

        // Update active power-ups
        val powerUpIter = activePowerUps.entries.iterator()
        while (powerUpIter.hasNext()) {
            val entry = powerUpIter.next()
            val remaining = entry.value - dtSeconds
            if (remaining <= 0f) {
                powerUpIter.remove()
                when (entry.key) {
                    PowerUpType.WIDE_KNIFE -> knifeWidthMultiplier = 1.0f
                    PowerUpType.NO_MESS_SHIELD -> noMessShieldActive = false
                    PowerUpType.AUTO_SPREAD -> autoSpreadActive = false
                    PowerUpType.INFINITE_BUTTER -> infiniteButterActive = false
                    PowerUpType.GOLDEN_MELT -> goldenMeltActive = false
                    PowerUpType.BUTTER_MAGNET -> butterMagnetActive = false
                }
            } else {
                entry.setValue(remaining)
            }
        }

        // Butter Magnet attracts floating power-up tokens toward knife
        if (butterMagnetActive) {
            for (token in powerUpTokens) {
                val dx = knifePos.x - token.x
                val dy = knifePos.y - token.y
                val dist = hypot(dx, dy)
                if (dist > 0.01f) {
                    token.x += (dx / dist) * dtSeconds * 0.40f
                    token.y += (dy / dist) * dtSeconds * 0.40f
                }
            }
            if (Random.nextFloat() < 0.30f && particles.size < 35) {
                particles.add(
                    ToastParticle(
                        x = knifePos.x + (Random.nextFloat() - 0.5f) * 0.14f,
                        y = knifePos.y + (Random.nextFloat() - 0.5f) * 0.14f,
                        vx = (Random.nextFloat() - 0.5f) * 0.03f,
                        vy = -0.04f,
                        alpha = 0.8f,
                        size = 7f + Random.nextFloat() * 5f,
                        color = Color(0xFFE040FB),
                        isGlow = true
                    )
                )
            }
        }

        // Infinite Butter continuous replenishment
        if (infiniteButterActive) {
            butterOnBlade = 1.0f
            if (Random.nextFloat() < 0.25f && particles.size < 30) {
                particles.add(
                    ToastParticle(
                        x = knifePos.x + (Random.nextFloat() - 0.5f) * 0.10f,
                        y = knifePos.y + (Random.nextFloat() - 0.5f) * 0.10f,
                        vx = (Random.nextFloat() - 0.5f) * 0.02f,
                        vy = -0.04f,
                        alpha = 0.8f,
                        size = 8f + Random.nextFloat() * 6f,
                        color = Color(0xFFFFD700),
                        isGlow = true
                    )
                )
            }
        }

        // Auto-spread powerup behavior: sweeping broad coverage that actually coats toast
        if (autoSpreadActive) {
            butterOnBlade = 1.0f
            autoSpreadAngle += dtSeconds * 3.6f

            // Wide Lissajous curve reaching across the toast surface
            val sweepX = (0.5f + cos(autoSpreadAngle) * 0.30f).coerceIn(0.18f, 0.82f)
            val sweepY = (0.5f + sin(autoSpreadAngle * 1.6f) * 0.26f).coerceIn(0.18f, 0.82f)
            val targetPos = Offset(sweepX, sweepY)
            onDrag(targetPos)

            // Directly deposit butter into cells around the sweep point
            val gx = (targetPos.x * gridWidth).toInt().coerceIn(0, gridWidth - 1)
            val gy = (targetPos.y * gridHeight).toInt().coerceIn(0, gridHeight - 1)
            for (ox in -2..2) {
                for (oy in -2..2) {
                    val cx = gx + ox
                    val cy = gy + oy
                    if (cx in 0 until gridWidth && cy in 0 until gridHeight && breadMask[cx][cy]) {
                        val cell = grid[cx][cy]
                        cell.butter = (cell.butter + 0.22f).coerceAtMost(1.3f)
                        cell.melt = (cell.melt + 0.15f).coerceAtMost(1.0f)
                    }
                }
            }

            // Magical green/gold glint sparkles
            if (Random.nextFloat() < 0.35f && particles.size < 30) {
                particles.add(
                    ToastParticle(
                        x = targetPos.x + (Random.nextFloat() - 0.5f) * 0.12f,
                        y = targetPos.y + (Random.nextFloat() - 0.5f) * 0.12f,
                        vx = (Random.nextFloat() - 0.5f) * 0.03f,
                        vy = -0.05f,
                        alpha = 0.75f,
                        size = 10f + Random.nextFloat() * 6f,
                        color = Color(0xFF69F0AE),
                        isGlow = true
                    )
                )
            }
        }

        // Token spawn timer
        tokenSpawnTimer -= dtSeconds
        if (tokenSpawnTimer <= 0f) {
            spawnRandomPowerUpToken()
            tokenSpawnTimer = 18f + Random.nextFloat() * 8f
        }

        // Melt butter on warm toast
        val baseMeltSpeed = level.toastTemp * 0.28f * (if (goldenMeltActive) 3.5f else 1.0f)
        val steamChance = level.toastTemp * 0.18f

        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                if (breadMask[x][y]) {
                    val cell = grid[x][y]
                    if (cell.butter > 0.05f) {
                        cell.melt = (cell.melt + dtSeconds * baseMeltSpeed).coerceAtMost(1.0f)
                    }
                }
            }
        }

        // Steam particles
        if (steamChance > 0.05f && Random.nextFloat() < steamChance && particles.size < 25) {
            particles.add(
                ToastParticle(
                    x = 0.3f + Random.nextFloat() * 0.4f,
                    y = 0.3f + Random.nextFloat() * 0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 0.02f,
                    vy = -0.06f - Random.nextFloat() * 0.04f,
                    alpha = 0.5f,
                    size = 12f + Random.nextFloat() * 8f,
                    color = Color.White.copy(alpha = 0.35f),
                    isSteam = true
                )
            )
        }

        // Update particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dtSeconds
            p.y += p.vy * dtSeconds
            p.alpha -= dtSeconds * (if (p.isSteam) 0.35f else 1.2f)
            p.size += dtSeconds * 4f
            if (p.alpha <= 0f) {
                pIter.remove()
            }
        }

        // Update score floating popups
        val popIter = scorePopups.iterator()
        while (popIter.hasNext()) {
            val pop = popIter.next()
            pop.offsetY += dtSeconds * 30f
            pop.alpha -= dtSeconds * 0.75f
            if (pop.alpha <= 0f) {
                popIter.remove()
            }
        }
        redrawTick++
    }

    private fun spawnButterSparks(x: Float, y: Float, count: Int, color: Color) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = 0.05f + Random.nextFloat() * 0.12f
            particles.add(
                ToastParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 0.85f,
                    size = 5f + Random.nextFloat() * 4f,
                    color = color
                )
            )
        }
    }

    fun recomputeMetrics() {
        var validBreadCells = 0
        var coveredCells = 0
        var totalThickness = 0f
        val thicknesses = mutableListOf<Float>()

        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                if (breadMask[x][y]) {
                    validBreadCells++
                    val b = grid[x][y].butter
                    if (b >= 0.12f) {
                        coveredCells++
                        totalThickness += b
                        thicknesses.add(b)
                    }
                }
            }
        }

        if (validBreadCells > 0) {
            currentCoverage = coveredCells.toFloat() / validBreadCells
        } else {
            currentCoverage = 0f
        }

        if (thicknesses.size > 10) {
            val mean = totalThickness / thicknesses.size
            var varianceSum = 0f
            for (t in thicknesses) {
                varianceSum += (t - mean) * (t - mean)
            }
            val stdDev = sqrt(varianceSum / thicknesses.size)
            currentEvenness = (1.0f - (stdDev / 0.7f)).coerceIn(0f, 1f)
        } else {
            currentEvenness = 0f
        }
    }

    fun finishGame(): GameResult {
        isFinished = true
        recomputeMetrics()

        val coverage = currentCoverage
        val evenness = currentEvenness
        val passedTears = tearsCount <= level.maxTearsAllowed

        // Uneven buttering minor end penalty only on extreme roughness
        if (coverage > 0.4f && evenness < 0.30f) {
            val unevenDeduction = ((0.30f - evenness) * 100f).roundToInt()
            currentScore = (currentScore - unevenDeduction).coerceAtLeast(0f)
        }

        // Perfection Bonus
        if (coverage >= 0.75f && evenness >= 0.60f && passedTears) {
            perfectionBonusTotal = 500
            currentScore += perfectionBonusTotal
        } else if (coverage >= level.targetCoverage && passedTears) {
            perfectionBonusTotal = 250
            currentScore += perfectionBonusTotal
        }

        val stars = when {
            !passedTears -> 0
            coverage >= level.star3Coverage -> 3
            coverage >= level.star2Coverage -> 2
            coverage >= level.star1Coverage -> 1
            else -> 0
        }

        val passed = stars >= 1

        // Coins earned: stars * 50 + score / 10
        val baseCoins = stars * 50 + (currentScore / 10f).roundToInt()
        val finalCoins = if (knifeSkin == KnifeSkin.RAINBOW_CHROME) (baseCoins * 1.5f).roundToInt() else baseCoins

        val ratingTitle = when (stars) {
            3 -> "Buttery Perfection!"
            2 -> "Golden Crisp Toast!"
            1 -> "Good Spread!"
            else -> if (!passedTears) "Torn To Shreds!" else "Needs More Butter!"
        }

        val feedbackMessage = when {
            !passedTears -> "You ripped ${tearsCount} hole(s) in the bread! Be gentler with cold butter."
            stars == 3 -> "Flawless glide! Gorgeous, even golden butter glaze from crust to crust."
            stars == 2 -> "Great coverage! Spread just a little more evenly for all 3 stars."
            stars == 1 -> "Decent buttering, but lots of bare spots remain on the toast."
            else -> "Coverage was ${(coverage * 100).toInt()}% (target was ${(level.targetCoverage * 100).toInt()}%)."
        }

        if (passed) {
            haptics.vibrateSuccess()
            audio.playStarSound(stars)
        }

        return GameResult(
            levelId = level.id,
            finalScore = currentScore.roundToInt(),
            initialScore = initialScore,
            timeScoreLoss = timeScoreLoss.roundToInt(),
            boundaryPenalties = boundaryPenaltiesTotal,
            tearPenalties = tearPenaltiesTotal,
            dropPenalties = dropPenaltiesTotal,
            perfectionBonus = perfectionBonusTotal,
            coverage = coverage,
            evenness = evenness,
            tearsCount = tearsCount,
            stars = stars,
            coinsEarned = finalCoins,
            passed = passed,
            ratingTitle = ratingTitle,
            feedbackMessage = feedbackMessage
        )
    }
}
