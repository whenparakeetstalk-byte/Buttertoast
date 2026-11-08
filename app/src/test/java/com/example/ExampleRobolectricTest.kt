package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LevelRepository
import com.example.model.BreadType
import com.example.model.KnifeSkin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Butter Toast", appName)
  }

  @Test
  fun `verify levels progression loaded`() {
    val levels = LevelRepository.levels
    assertEquals(21, levels.size)
    val tutorialLevel = levels.first { it.id == 0 }
    assertEquals("Toast Academy: The Basics", tutorialLevel.title)
    assertTrue(tutorialLevel.targetCoverage == 0.40f)
    val level1 = levels.first { it.id == 1 }
    assertTrue(level1.targetCoverage > 0.5f)
    val level10 = levels.first { it.id == 10 }
    assertTrue(level10.targetCoverage >= 0.80f)

    val rosaLevel = levels.first { it.id == 15 }
    assertEquals("Rosa Parks Butters Bread", rosaLevel.title)
    assertTrue(rosaLevel.isBusRide)

    val georgeLevel = levels.first { it.id == 16 }
    assertEquals("George's Banana Mayo Sandwich", georgeLevel.title)
    assertTrue(georgeLevel.hasBananas)
    assertEquals(com.example.model.ButterType.BANANA_MAYO, georgeLevel.butterType)

    val helenLevel = levels.first { it.id == 17 }
    assertEquals("Helen Keller Butters Bread", helenLevel.title)
    assertTrue(helenLevel.isHapticOnly)
    assertEquals(BreadType.WHITE, helenLevel.breadType)
    assertEquals(1, helenLevel.patCount)

    val helenBagelLevel = levels.first { it.id == 19 }
    assertEquals("Helen Keller: The Tiny Board Bagel", helenBagelLevel.title)
    assertTrue(helenBagelLevel.isHapticOnly)
    assertTrue(helenBagelLevel.isSmallBoard)
    assertEquals(BreadType.BAGEL, helenBagelLevel.breadType)

    val ceramicLevel = levels.first { it.id == 20 }
    assertEquals("The Melbourne Ceramic Ring", ceramicLevel.title)
    assertTrue(ceramicLevel.isCeramicPlate)
    assertEquals(BreadType.CERAMIC_OVAL, ceramicLevel.breadType)
    assertEquals(com.example.model.ButterType.VEGEMITE, ceramicLevel.butterType)
  }

  @Test
  fun `verify knives, breads, powerups, and point upgrades have valid properties`() {
    assertTrue(KnifeSkin.entries.isNotEmpty())
    assertTrue(BreadType.entries.isNotEmpty())
    assertTrue(com.example.model.PowerUpType.entries.any { it.id == "butter_magnet" })
    assertTrue(com.example.model.PointUpgrade.ALL_UPGRADES.isNotEmpty())
  }
}
