package com.example.zapmessage

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun navigation_navigateToFavoritesAndProfile_showsCorrectScreens() {
        onView(withText("Mandar mensagem")).check(matches(isDisplayed()))
        composeTestRule.onNodeWithText(AppDestinations.RECENTS.label).performClick()
        onView(withText("Numeros Recentes")).check(matches(isDisplayed()))
        composeTestRule.onNodeWithText(AppDestinations.PROFILE.label).performClick()
        onView(withText("Profile Screen")).check(matches(isDisplayed()))
    }

    @Test
    fun homeScreen_whenValidNumberAndButtonClick_launchesWhatsAppIntent() {
        val phoneNumber = "11953870147"

        onView(withId(R.id.inputText)).perform(typeText(phoneNumber))
        onView(withText("Mandar mensagem")).perform(click())
        Intents.intended(IntentMatchers.hasData("https://wa.me/55$phoneNumber"))
    }

    @Test
    fun favoritesScreen_displaysListOfNumbers() {
        composeTestRule.onNodeWithText(AppDestinations.RECENTS.label).performClick()
        onView(withText("Numeros Recentes")).check(matches(isDisplayed()))
    }
}
