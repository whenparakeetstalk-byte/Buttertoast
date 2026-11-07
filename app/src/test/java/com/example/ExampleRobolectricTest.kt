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
@Config(sdk = [36])
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
    assertEquals(11, levels.size)
    val tutorialLevel = levels.first { it.id == 0 }
    assertEquals("Toast Academy: The Basics", tutorialLevel.title)
    assertTrue(tutorialLevel.targetCoverage == 0.40f)
    val level1 = levels.first { it.id == 1 }
    assertTrue(level1.targetCoverage > 0.5f)
    assertTrue(levels.last().targetCoverage >= 0.90f)
  }

  @Test
  fun `verify knives, breads, and powerups have valid properties`() {
    assertTrue(KnifeSkin.entries.isNotEmpty())
    assertTrue(BreadType.entries.isNotEmpty())
    assertTrue(com.example.model.PowerUpType.entries.any { it.id == "butter_magnet" })
  }
}
