package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.ToastEngine
import com.example.model.BreadType
import com.example.model.ButterType
import com.example.model.PowerUpType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ToastBoardCanvas(
    engine: ToastEngine,
    modifier: Modifier = Modifier
) {
    // Observe reactive state
    val tick = engine.redrawTick
    val currentKnife = engine.knifePos

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(engine) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startX = (down.position.x / size.width).coerceIn(0f, 1f)
                        val startY = (down.position.y / size.height).coerceIn(0f, 1f)
                        engine.onDragStart(Offset(startX, startY))

                        var pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull()
                            if (change == null || !change.pressed) {
                                break
                            }
                            change.consume()
                            val currentX = (change.position.x / size.width).coerceIn(0f, 1f)
                            val currentY = (change.position.y / size.height).coerceIn(0f, 1f)
                            engine.onDrag(Offset(currentX, currentY))
                        }
                        engine.onDragEnd()
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Wooden Cutting Board Background
        drawCuttingBoard(w, h)

        // 2. Draw Toast (Crust, Crumb, Holes/Waffle grid)
        drawToastBread(w, h, engine)

        // 3. Draw No-Mess Shield Aura if active
        if (engine.noMessShieldActive) {
            drawShieldAura(w, h)
        }

        // 4. Draw Butter Layer (Spreads, Melting Gloss, Flecks)
        drawButterLayer(w, h, engine)

        // 5. Draw Bread Tears
        drawBreadTears(w, h, engine)

        // 6. Draw Butter Pats sitting on toast
        drawButterPats(w, h, engine)

        // 7. Draw in-level Power-Up Tokens to collect
        drawPowerUpTokens(w, h, engine)

        // 8. Draw Steam & Butter Sparkle Particles
        drawParticles(w, h, engine)

        // 9. Draw The Knife (with handle, blade, and butter on blade)
        drawKnife(w, h, engine)

        // 10. Draw Floating Score & Bonus Popups
        drawFloatingPopups(w, h, engine)

        // 11. Initial Drag Guide (if not yet spreading)
        if (!engine.isDragging && engine.currentCoverage == 0f) {
            drawDragGuide(w, h, engine)
        }
    }
}

private fun DrawScope.drawCuttingBoard(w: Float, h: Float) {
    val boardPadding = 16f
    val boardRect = Size(w - boardPadding * 2, h - boardPadding * 2)

    // Board Drop Shadow
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(boardPadding + 6f, boardPadding + 10f),
        size = boardRect,
        cornerRadius = CornerRadius(36f, 36f)
    )

    // Wood Base
    val woodBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE2C499),
            Color(0xFFD6B283),
            Color(0xFFC79E6F),
            Color(0xFFB58A59)
        )
    )
    drawRoundRect(
        brush = woodBrush,
        topLeft = Offset(boardPadding, boardPadding),
        size = boardRect,
        cornerRadius = CornerRadius(32f, 32f)
    )

    // Wood Planks / Grain lines
    val plankSpacing = boardRect.height / 8f
    for (i in 1..7) {
        val y = boardPadding + i * plankSpacing
        drawLine(
            color = Color(0x225C381E),
            start = Offset(boardPadding + 20f, y),
            end = Offset(w - boardPadding - 20f, y),
            strokeWidth = 2.5f
        )
        drawLine(
            color = Color(0x18FFFFFF),
            start = Offset(boardPadding + 20f, y + 2.5f),
            end = Offset(w - boardPadding - 20f, y + 2.5f),
            strokeWidth = 1.5f
        )
    }

    // Board Bevel Border
    drawRoundRect(
        color = Color(0x3D754C24),
        topLeft = Offset(boardPadding + 14f, boardPadding + 14f),
        size = Size(boardRect.width - 28f, boardRect.height - 28f),
        cornerRadius = CornerRadius(22f, 22f),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawShieldAura(w: Float, h: Float) {
    val toastLeft = w * 0.10f
    val toastTop = h * 0.10f
    val toastWidth = w * 0.80f
    val toastHeight = h * 0.80f

    val auraBrush = Brush.radialGradient(
        colors = listOf(
            Color(0x5500E5FF),
            Color(0x2200E5FF),
            Color(0x0000E5FF)
        ),
        center = Offset(w * 0.5f, h * 0.5f),
        radius = toastWidth * 0.6f
    )
    drawOval(
        brush = auraBrush,
        topLeft = Offset(toastLeft, toastTop),
        size = Size(toastWidth, toastHeight)
    )
    drawRoundRect(
        color = Color(0x8800E5FF),
        topLeft = Offset(toastLeft + 8f, toastTop + 8f),
        size = Size(toastWidth - 16f, toastHeight - 16f),
        cornerRadius = CornerRadius(40f, 40f),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawToastBread(w: Float, h: Float, engine: ToastEngine) {
    val bread = engine.level.breadType
    val toastTemp = engine.level.toastTemp

    val toastLeft = w * 0.12f
    val toastTop = h * 0.12f
    val toastWidth = w * 0.76f
    val toastHeight = h * 0.76f

    // Toast Shadow
    drawRoundRect(
        color = Color(0x40000000),
        topLeft = Offset(toastLeft + 8f, toastTop + 16f),
        size = Size(toastWidth, toastHeight),
        cornerRadius = CornerRadius(toastWidth * 0.15f, toastWidth * 0.15f)
    )

    when (bread) {
        BreadType.BAGEL -> {
            val center = Offset(w * 0.5f, h * 0.5f)
            val outerRadius = toastWidth * 0.45f
            val innerRadius = toastWidth * 0.12f

            drawCircle(color = bread.crustColor, radius = outerRadius, center = center)
            val bagelBrush = Brush.radialGradient(
                colors = listOf(bread.toastedCrumbColor, bread.crumbColor),
                center = center,
                radius = outerRadius
            )
            drawCircle(brush = bagelBrush, radius = outerRadius - 16f, center = center)
            drawCircle(color = Color(0xFFD6B283), radius = innerRadius, center = center)
            drawCircle(
                color = bread.crustColor,
                radius = innerRadius + 6f,
                center = center,
                style = Stroke(width = 8f)
            )
        }
        BreadType.SOURDOUGH -> {
            val ovalSize = Size(toastWidth * 0.94f, toastHeight * 0.82f)
            val ovalTopLeft = Offset(w * 0.5f - ovalSize.width * 0.5f, h * 0.5f - ovalSize.height * 0.5f)

            drawOval(color = bread.crustColor, topLeft = ovalTopLeft, size = ovalSize)
            val crumbBrush = Brush.radialGradient(
                colors = listOf(bread.crumbColor, bread.toastedCrumbColor, bread.crustColor),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = ovalSize.width * 0.55f
            )
            drawOval(
                brush = crumbBrush,
                topLeft = Offset(ovalTopLeft.x + 14f, ovalTopLeft.y + 14f),
                size = Size(ovalSize.width - 28f, ovalSize.height - 28f)
            )

            val holes = listOf(
                Offset(w * 0.38f, h * 0.42f) to 14f,
                Offset(w * 0.62f, h * 0.45f) to 18f,
                Offset(w * 0.46f, h * 0.58f) to 12f,
                Offset(w * 0.55f, h * 0.35f) to 10f
            )
            for ((pos, r) in holes) {
                drawCircle(color = Color(0x334E270E), radius = r, center = pos)
                drawCircle(color = Color(0x22000000), radius = r * 0.7f, center = pos)
            }
        }
        BreadType.WAFFLE -> {
            val waffleRect = Size(toastWidth * 0.88f, toastHeight * 0.88f)
            val topLeft = Offset(w * 0.5f - waffleRect.width * 0.5f, h * 0.5f - waffleRect.height * 0.5f)

            drawRoundRect(
                color = bread.crustColor,
                topLeft = topLeft,
                size = waffleRect,
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = bread.crumbColor,
                topLeft = Offset(topLeft.x + 10f, topLeft.y + 10f),
                size = Size(waffleRect.width - 20f, waffleRect.height - 20f),
                cornerRadius = CornerRadius(18f, 18f)
            )

            val gridCount = 4
            val cellW = (waffleRect.width - 28f) / gridCount
            val cellH = (waffleRect.height - 28f) / gridCount

            for (gx in 0 until gridCount) {
                for (gy in 0 until gridCount) {
                    val px = topLeft.x + 14f + gx * cellW + 4f
                    val py = topLeft.y + 14f + gy * cellH + 4f
                    val pocketSize = Size(cellW - 8f, cellH - 8f)

                    drawRoundRect(
                        color = Color(0x3B6D3600),
                        topLeft = Offset(px, py),
                        size = pocketSize,
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = bread.toastedCrumbColor,
                        topLeft = Offset(px + 3f, py + 3f),
                        size = Size(pocketSize.width - 6f, pocketSize.height - 6f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
        else -> {
            val sliceRect = Size(toastWidth * 0.88f, toastHeight * 0.84f)
            val topLeft = Offset(w * 0.5f - sliceRect.width * 0.5f, h * 0.5f - sliceRect.height * 0.5f)
            val cornerRadius = CornerRadius(sliceRect.width * 0.22f, sliceRect.width * 0.22f)

            drawRoundRect(
                color = bread.crustColor,
                topLeft = topLeft,
                size = sliceRect,
                cornerRadius = cornerRadius
            )

            val crumbBrush = Brush.radialGradient(
                colors = listOf(bread.crumbColor, bread.toastedCrumbColor),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = sliceRect.width * 0.55f
            )
            drawRoundRect(
                brush = crumbBrush,
                topLeft = Offset(topLeft.x + 12f, topLeft.y + 12f),
                size = Size(sliceRect.width - 24f, sliceRect.height - 24f),
                cornerRadius = CornerRadius(sliceRect.width * 0.18f, sliceRect.width * 0.18f)
            )

            if (toastTemp > 0.4f) {
                val toastGlow = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x288B4513),
                        Color(0x38D2691E),
                        Color(0x188B4513)
                    ),
                    startY = topLeft.y + 30f,
                    endY = topLeft.y + sliceRect.height - 30f
                )
                drawRoundRect(
                    brush = toastGlow,
                    topLeft = Offset(topLeft.x + 12f, topLeft.y + 12f),
                    size = Size(sliceRect.width - 24f, sliceRect.height - 24f),
                    cornerRadius = CornerRadius(sliceRect.width * 0.18f, sliceRect.width * 0.18f)
                )
            }
        }
    }
}

private fun DrawScope.drawButterLayer(w: Float, h: Float, engine: ToastEngine) {
    val butterType = engine.level.butterType
    val gw = engine.gridWidth
    val gh = engine.gridHeight
    val cellW = w / gw
    val cellH = h / gh

    for (x in 0 until gw) {
        for (y in 0 until gh) {
            val cell = engine.grid[x][y]
            if (cell.butter > 0.05f && engine.breadMask[x][y]) {
                val cx = (x + 0.5f) * cellW
                val cy = (y + 0.5f) * cellH
                val radius = (cellW * 0.72f * (0.8f + cell.butter * 0.35f)).coerceIn(cellW * 0.55f, cellW * 0.95f)

                val melt = cell.melt.coerceIn(0f, 1f)
                val baseColor = lerpColor(butterType.solidColor, butterType.meltedColor, melt)
                val alpha = (0.50f + cell.butter * 0.45f).coerceIn(0.35f, 0.98f)

                val butterBrush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = alpha),
                        baseColor.copy(alpha = alpha * 0.85f),
                        baseColor.copy(alpha = alpha * 0.35f),
                        baseColor.copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = radius
                )
                drawCircle(brush = butterBrush, radius = radius, center = Offset(cx, cy))

                if (melt > 0.35f && cell.butter > 0.4f) {
                    val shineAlpha = (melt * 0.45f).coerceAtMost(0.5f)
                    drawCircle(
                        color = butterType.shineColor.copy(alpha = shineAlpha),
                        radius = radius * 0.35f,
                        center = Offset(cx - radius * 0.2f, cy - radius * 0.2f)
                    )
                }

                if (cell.flecks && butterType.hasFlecks) {
                    drawCircle(
                        color = butterType.fleckColor.copy(alpha = 0.8f),
                        radius = 2.5f,
                        center = Offset(cx + (cell.crumbNoise - 0.5f) * 8f, cy + (cell.crumbNoise - 0.5f) * 8f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBreadTears(w: Float, h: Float, engine: ToastEngine) {
    val gw = engine.gridWidth
    val gh = engine.gridHeight
    val cellW = w / gw
    val cellH = h / gh

    for (x in 0 until gw) {
        for (y in 0 until gh) {
            val cell = engine.grid[x][y]
            if (cell.isTorn && engine.breadMask[x][y]) {
                val cx = (x + 0.5f) * cellW
                val cy = (y + 0.5f) * cellH

                drawCircle(color = Color(0x992B1408), radius = cellW * 0.75f, center = Offset(cx, cy))
                drawCircle(color = Color(0xDD120803), radius = cellW * 0.45f, center = Offset(cx, cy))
                drawLine(
                    color = Color(0xFF6E260E),
                    start = Offset(cx - cellW * 0.6f, cy - cellH * 0.4f),
                    end = Offset(cx + cellW * 0.5f, cy + cellH * 0.5f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawButterPats(w: Float, h: Float, engine: ToastEngine) {
    val butterType = engine.level.butterType

    for (pat in engine.butterPats) {
        if (pat.volume > 0.05f) {
            val px = pat.x * w
            val py = pat.y * h
            val patWidth = w * pat.initialSize * (0.6f + pat.volume * 0.4f)
            val patHeight = patWidth * 0.62f

            val patTopLeft = Offset(px - patWidth * 0.5f, py - patHeight * 0.5f)

            drawRoundRect(
                color = Color(0x35000000),
                topLeft = Offset(patTopLeft.x + 4f, patTopLeft.y + 8f),
                size = Size(patWidth, patHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                color = butterType.solidColor,
                topLeft = patTopLeft,
                size = Size(patWidth, patHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            drawRoundRect(
                color = Color(0x80FFFFFF),
                topLeft = Offset(patTopLeft.x + 3f, patTopLeft.y + 3f),
                size = Size(patWidth - 6f, patHeight * 0.38f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            if (butterType.hasFlecks) {
                drawCircle(color = butterType.fleckColor, radius = 2.5f, center = Offset(px - 6f, py - 2f))
                drawCircle(color = butterType.fleckColor, radius = 2.2f, center = Offset(px + 8f, py + 3f))
            }
        }
    }
}

private fun DrawScope.drawPowerUpTokens(w: Float, h: Float, engine: ToastEngine) {
    for (token in engine.powerUpTokens) {
        val tx = token.x * w
        val ty = token.y * h
        val radius = 24f

        // Glowing outer pulse aura
        drawCircle(
            color = token.type.iconColor.copy(alpha = 0.35f),
            radius = radius + 8f,
            center = Offset(tx, ty)
        )
        // Token disk
        drawCircle(
            color = Color(0xFF212121),
            radius = radius,
            center = Offset(tx, ty)
        )
        drawCircle(
            color = token.type.iconColor,
            radius = radius - 4f,
            center = Offset(tx, ty)
        )
        // Center white star / glint
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(tx - 3f, ty - 3f)
        )
    }
}

private fun DrawScope.drawParticles(w: Float, h: Float, engine: ToastEngine) {
    for (p in engine.particles) {
        val px = p.x * w
        val py = p.y * h
        drawCircle(
            color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
            radius = p.size,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawKnife(w: Float, h: Float, engine: ToastEngine) {
    val skin = engine.knifeSkin
    val butterType = engine.level.butterType
    val kx = engine.knifePos.x * w
    val ky = engine.knifePos.y * h

    rotate(degrees = engine.knifeAngle, pivot = Offset(kx, ky)) {
        val widthMult = engine.knifeWidthMultiplier
        val lengthMult = 1.0f + (widthMult - 1.0f) * 0.60f
        val bladeLength = skin.bladeLength * lengthMult
        val bladeWidth = skin.bladeWidth * widthMult
        val handleLength = 110f
        val handleWidth = 22f

        // 3D tactile lift & drop shadow: lifted when finger is up, contact shadow when pressed down
        val isDown = engine.isDragging
        val shadowOffset = if (isDown) Offset(4f, 6f) else Offset(14f, 20f)
        val shadowAlpha = if (isDown) 0.38f else 0.22f

        // Golden aura if Infinite Butter is active
        if (engine.infiniteButterActive) {
            drawKnifeSilhouette(
                baseX = kx,
                baseY = ky,
                bladeLength = bladeLength + 10f,
                bladeWidth = bladeWidth + 12f,
                handleLength = handleLength,
                handleWidth = handleWidth + 4f,
                bladeColor = Color(0x66FFD700),
                handleColor = Color(0x33FFD700),
                accentColor = Color(0x88FFF176)
            )
        }

        drawKnifeSilhouette(
            baseX = kx + shadowOffset.x,
            baseY = ky + shadowOffset.y,
            bladeLength = bladeLength,
            bladeWidth = bladeWidth,
            handleLength = handleLength,
            handleWidth = handleWidth,
            bladeColor = Color(0xFF000000).copy(alpha = shadowAlpha),
            handleColor = Color(0xFF000000).copy(alpha = shadowAlpha),
            accentColor = Color.Transparent
        )

        drawKnifeSilhouette(
            baseX = kx,
            baseY = ky,
            bladeLength = bladeLength,
            bladeWidth = bladeWidth,
            handleLength = handleLength,
            handleWidth = handleWidth,
            bladeColor = skin.bladeColor,
            handleColor = skin.handleColor,
            accentColor = skin.accentColor
        )

        // Butter slather on knife edge: sleek, realistic ribbon
        if (engine.butterOnBlade > 0.02f) {
            val butterAmount = engine.butterOnBlade.coerceIn(0f, 1f)
            val slatherLength = bladeLength * (0.22f + butterAmount * 0.40f)
            val slatherWidth = (3.5f + 4.5f * butterAmount * engine.knifeWidthMultiplier).coerceAtMost(10f)
            val butterStart = kx + bladeLength * 0.10f

            val butterPath = Path().apply {
                moveTo(butterStart, ky + bladeWidth * 0.42f)
                lineTo(butterStart + slatherLength, ky + bladeWidth * 0.42f)
                quadraticTo(
                    butterStart + slatherLength + 4f,
                    ky + bladeWidth * 0.42f + slatherWidth,
                    butterStart + slatherLength * 0.5f,
                    ky + bladeWidth * 0.42f + slatherWidth
                )
                quadraticTo(
                    butterStart - 2f,
                    ky + bladeWidth * 0.42f + slatherWidth * 0.7f,
                    butterStart,
                    ky + bladeWidth * 0.42f
                )
                close()
            }
            drawPath(
                path = butterPath,
                color = butterType.solidColor
            )
            drawLine(
                color = Color(0x99FFFFFF),
                start = Offset(butterStart + 3f, ky + bladeWidth * 0.42f + 1.5f),
                end = Offset(butterStart + slatherLength * 0.8f, ky + bladeWidth * 0.42f + 1.5f),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawKnifeSilhouette(
    baseX: Float,
    baseY: Float,
    bladeLength: Float,
    bladeWidth: Float,
    handleLength: Float,
    handleWidth: Float,
    bladeColor: Color,
    handleColor: Color,
    accentColor: Color
) {
    val bladePath = Path().apply {
        moveTo(baseX - 10f, baseY - bladeWidth * 0.45f)
        lineTo(baseX + bladeLength - 20f, baseY - bladeWidth * 0.40f)
        cubicTo(
            baseX + bladeLength + 5f, baseY - bladeWidth * 0.2f,
            baseX + bladeLength + 5f, baseY + bladeWidth * 0.4f,
            baseX + bladeLength - 15f, baseY + bladeWidth * 0.45f
        )
        lineTo(baseX - 10f, baseY + bladeWidth * 0.45f)
        close()
    }

    drawPath(path = bladePath, color = bladeColor)

    drawLine(
        color = Color(0x60FFFFFF),
        start = Offset(baseX, baseY - bladeWidth * 0.35f),
        end = Offset(baseX + bladeLength - 22f, baseY - bladeWidth * 0.30f),
        strokeWidth = 2.5f
    )

    val bolsterWidth = 14f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(baseX - 18f, baseY - bladeWidth * 0.5f),
        size = Size(bolsterWidth, bladeWidth),
        cornerRadius = CornerRadius(4f, 4f)
    )

    val handleLeft = baseX - 18f - handleLength
    val handleTop = baseY - handleWidth * 0.5f
    drawRoundRect(
        color = handleColor,
        topLeft = Offset(handleLeft, handleTop),
        size = Size(handleLength, handleWidth),
        cornerRadius = CornerRadius(handleWidth * 0.45f, handleWidth * 0.45f)
    )

    val rivetR = 3.5f
    drawCircle(color = accentColor, radius = rivetR, center = Offset(handleLeft + handleLength * 0.25f, baseY))
    drawCircle(color = accentColor, radius = rivetR, center = Offset(handleLeft + handleLength * 0.70f, baseY))
}

private fun DrawScope.drawFloatingPopups(w: Float, h: Float, engine: ToastEngine) {
    for (pop in engine.scorePopups) {
        val px = pop.x * w
        val py = pop.y * h - pop.offsetY
        val alpha = pop.alpha.coerceIn(0f, 1f)
        val textColor = if (pop.isPositive) android.graphics.Color.parseColor("#2E7D32") else android.graphics.Color.parseColor("#D32F2F")

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = textColor
                this.alpha = (alpha * 255).toInt()
                textSize = 34f
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 2f, 2f, android.graphics.Color.argb((alpha * 120).toInt(), 0, 0, 0))
            }
            drawText(pop.text, px, py, paint)
        }
    }
}

private fun DrawScope.drawDragGuide(w: Float, h: Float, engine: ToastEngine) {
    val kx = engine.knifePos.x * w
    val ky = engine.knifePos.y * h

    // Pulsing halo ring around knife
    drawCircle(
        color = Color(0x66F59E0B),
        radius = 55f,
        center = Offset(kx, ky),
        style = Stroke(width = 3.5f)
    )

    // Guide text above knife
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#451A03")
            textSize = 32f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.WHITE)
        }
        drawText("Touch & drag anywhere to steer knife!", w * 0.5f, h * 0.92f, paint)
    }
}

private fun lerpColor(c1: Color, c2: Color, t: Float): Color {
    val factor = t.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * factor,
        green = c1.green + (c2.green - c1.green) * factor,
        blue = c1.blue + (c2.blue - c1.blue) * factor,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * factor
    )
}


