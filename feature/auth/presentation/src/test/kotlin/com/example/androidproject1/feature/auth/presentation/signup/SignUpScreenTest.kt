package com.example.androidproject1.feature.auth.presentation.signup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What is on screen, and what a tap does — the half `SignUpViewModelTest` cannot reach.
 *
 * **Everything is found by `testTag`, never by text.** Copy gets reworded and translated; a test
 * that finds a button by its label fails on a wording change that broke nothing.
 */

// Robolectric's default device is a 320 × 470 dp screen from 2012, and this form is five elements
// tall — the ghost button falls off the bottom of it and `assertIsDisplayed` rightly fails. The
// qualifier asks for an ordinary phone instead, which is what the app is drawn for.
private const val PHONE = "w411dp-h891dp"

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE)
class SignUpScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<SignUpEvent>()

    private fun render(state: SignUpState) {
        compose.setContent {
            AppTheme {
                SignUpScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the three fields and both buttons`() {
        render(SignUpState.PREVIEW)

        compose.onNodeWithTag("SignUpScreen").assertIsDisplayed()
        compose.onNodeWithTag("signUp_emailField").assertIsDisplayed()
        compose.onNodeWithTag("signUp_passwordField").assertIsDisplayed()
        compose.onNodeWithTag("signUp_confirmPasswordField").assertIsDisplayed()
        compose.onNodeWithTag("signUp_submitButton").assertIsDisplayed()
        compose.onNodeWithTag("signUp_loginButton").assertIsDisplayed()
    }

    @Test
    fun `submit is disabled until the form can be submitted`() {
        // canSubmit is derived: blank fields mean it cannot be submitted.
        render(SignUpState())

        compose.onNodeWithTag("signUp_submitButton").assertIsNotEnabled()
    }

    @Test
    fun `submit is disabled while the passwords differ`() {
        // The one rule that needs two fields, so it is checked on the state rather than by a
        // validator. The screen has to honour it, which is what this asserts.
        val mismatched = SignUpState.PREVIEW.confirmPassword.changed("hunter3!!")
        render(SignUpState.PREVIEW.copy(confirmPassword = mismatched))

        compose.onNodeWithTag("signUp_submitButton").assertIsNotEnabled()
    }

    @Test
    fun `submit is enabled once it can be`() {
        render(SignUpState.PREVIEW)

        compose.onNodeWithTag("signUp_submitButton").assertIsEnabled()
    }

    @Test
    fun `typing the confirmation reports it as an event`() {
        // AppTextField is a label, an input and a supporting line, and the caller's modifier goes
        // to the group — so a test that types reaches the input inside it.
        render(SignUpState())

        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("signUp_confirmPasswordField")))
            .performTextInput("a")

        assertEquals(listOf(SignUpEvent.ConfirmPasswordChanged("a")), events)
    }

    @Test
    fun `pressing submit reports it as an event`() {
        render(SignUpState.PREVIEW)

        compose.onNodeWithTag("signUp_submitButton").performClick()

        assertEquals(listOf(SignUpEvent.SignUpClicked), events)
    }

    @Test
    fun `pressing log in reports it as an event`() {
        render(SignUpState.PREVIEW)

        compose.onNodeWithTag("signUp_loginButton").performClick()

        assertEquals(listOf(SignUpEvent.LoginClicked), events)
    }

    @Test
    fun `the submit button can be scrolled to`() {
        render(SignUpState.PREVIEW)

        // performScrollTo throws when nothing above the node scrolls, which is what this form was:
        // a fixed centred column whose submit button is off-screen at a large font or under an
        // open keyboard.
        compose.onNodeWithTag("signUp_submitButton").performScrollTo().assertIsDisplayed()
    }
}
