package com.example.androidproject1.feature.profile.presentation.profile

import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.feature.profile.presentation.FakeProfileRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import com.example.androidproject1.service.core.ui.text.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What the state becomes, and what navigation is emitted.
 *
 * Robolectric, because a validator's message is the thing worth asserting and a `UiText` only
 * becomes words against a real resource table. Asserting "some error is set" would pass whichever
 * rule fired, which is the one thing these tests exist to tell apart.
 */
@RunWith(RobolectricTestRunner::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun viewModel(repository: FakeProfileRepository = FakeProfileRepository()) =
        ProfileViewModel(logger = FakeLogger(), profileRepository = repository)

    @Test
    fun `renders an empty form immediately, with no overlay`() = runTest {
        val state = viewModel().state.value

        assertEquals("", state.data!!.name.value)
        // A form is drawable before anything is loaded, so nothing sits behind a spinner.
        assertNull(state.loading)
        assertNull(state.alert)
    }

    @Test
    fun `fills the form from the stored profile, without shouting about it`() = runTest {
        val stored = Profile(name = "Jana", email = "jana@example.com", avatarUri = "file:///a.jpg")

        val state = viewModel(FakeProfileRepository(stored)).state.value.data!!

        assertEquals("Jana", state.name.value)
        assertEquals("jana@example.com", state.email.value)
        assertEquals("file:///a.jpg", state.avatarUri)
        // Untouched: a stored value is not something the user just typed, so no error is shown
        // even if the stored value breaks a rule.
        assertNull(state.name.error)
        assertNull(state.email.error)
    }

    @Test
    fun `a load that lands late does not overwrite what is being typed`() = runTest {
        val viewModel = viewModel(FakeProfileRepository(Profile("Stored", "stored@example.com", null)))

        viewModel.onUiEvent(ProfileEvent.NameChanged("Typing"))

        assertEquals("Typing", viewModel.state.value.data!!.name.value)
    }

    @Test
    fun `clearing the name reports it as required`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.NameChanged(""))

        assertEquals(
            "This field is required.",
            viewModel.state.value.data!!.name.error?.resolve(context),
        )
    }

    @Test
    fun `a one-letter name reports the minimum length`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.NameChanged("J"))

        assertEquals(
            "Must be at least 2 characters.",
            viewModel.state.value.data!!.name.error?.resolve(context),
        )
    }

    @Test
    fun `a name that meets the rules has no error`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.NameChanged("Jana"))

        assertNull(viewModel.state.value.data!!.name.error)
    }

    @Test
    fun `an address without a domain reports the email rule`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.EmailChanged("jana@"))

        assertEquals(
            "Enter a valid email address.",
            viewModel.state.value.data!!.email.error?.resolve(context),
        )
    }

    @Test
    fun `clearing the address reports it as required rather than malformed`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.EmailChanged(""))

        // `email` passes a blank value on purpose — saying "enter a valid address" about an empty
        // field describes the wrong problem.
        assertEquals(
            "This field is required.",
            viewModel.state.value.data!!.email.error?.resolve(context),
        )
    }

    @Test
    fun `the form cannot be submitted until both fields pass`() = runTest {
        val viewModel = viewModel()
        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(ProfileEvent.NameChanged("Jana"))
        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(ProfileEvent.EmailChanged("jana@example.com"))
        assertTrue(viewModel.state.value.data!!.canSubmit)
    }

    @Test
    fun `saving an untouched form reveals every error instead of doing nothing`() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(ProfileEvent.SaveClicked)

        val state = viewModel.state.value.data!!
        assertNotNull(state.name.error)
        assertNotNull(state.email.error)
        assertNull(repository.saved)
    }

    @Test
    fun `saving an invalid form says what is wrong`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(ProfileEvent.NameChanged("Jana"))
        viewModel.onUiEvent(ProfileEvent.EmailChanged("jana@"))

        viewModel.onUiEvent(ProfileEvent.SaveClicked)

        // The first thing wrong, in field order, repeated for whoever is looking at the button.
        assertEquals("Enter a valid email address.", viewModel.state.value.data!!.firstError?.resolve(context))
    }

    @Test
    fun `an address is trimmed as it is typed, so what is shown is what is validated`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.EmailChanged(" jana@example.com "))

        val state = viewModel.state.value.data!!
        assertEquals("jana@example.com", state.email.value)
        assertNull(state.email.error)
    }

    @Test
    fun `saving a valid form stores it, trimmed`() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(ProfileEvent.NameChanged("  Jana Nováková  "))
        viewModel.onUiEvent(ProfileEvent.EmailChanged(" jana@example.com "))

        viewModel.onUiEvent(ProfileEvent.SaveClicked)

        assertEquals(
            Profile(name = "Jana Nováková", email = "jana@example.com", avatarUri = null),
            repository.saved,
        )
    }

    @Test
    fun `asking for the camera opens the gated section and nothing else`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.TakePhotoClicked)

        // The permission itself is never in the state: it lives outside the app, so the screen
        // reads it in composition. All this flag says is which half of the picker is showing.
        assertTrue(viewModel.state.value.data!!.cameraOpen)
    }

    @Test
    fun `a picked picture is stored and replaces the avatar`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(ProfileEvent.TakePhotoClicked)

        viewModel.onUiEvent(ProfileEvent.AvatarPicked("content://media/picked"))

        val state = viewModel.state.value.data!!
        // What is kept is the copy the data layer made, never the URI that was handed in.
        assertEquals("file:///data/avatar/stored.jpg", state.avatarUri)
        assertFalse(state.cameraOpen)
    }

    @Test
    fun `up navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProfileEvent.NavigateUpClicked)

        assertEquals(ProfileNavigation.NavigateUp, viewModel.navigation.first())
    }

    /** B1U4: an edited profile asks before the back gesture throws the edit away. */
    @Test
    fun `back on an edited profile asks, and discarding navigates up`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(ProfileEvent.NameChanged("Jana"))
        assertTrue(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(ProfileEvent.BackRequested)
        assertEquals(ALERT_ID_DISCARD, viewModel.state.value.alert?.id)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(ALERT_ID_DISCARD, payload = null))
        assertNull(viewModel.state.value.alert)
        assertEquals(ProfileNavigation.NavigateUp, viewModel.navigation.first())
    }
}
