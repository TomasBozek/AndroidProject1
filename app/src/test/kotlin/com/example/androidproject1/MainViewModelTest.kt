package com.example.androidproject1

import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.test.FakeErrorTracker
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.Session
import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import com.example.androidproject1.feature.catalog.presentation.categories.CategoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import com.example.androidproject1.feature.catalog.presentation.products.ProductsDestination
import com.example.androidproject1.feature.home.presentation.home.HomeDestination
import com.example.androidproject1.feature.onboarding.domain.test.FakeOnboardingRepository
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.test.FakeThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = FakeLogger()
    private val authService = FakeAuthService()
    private val catalogRepository = FakeCatalogRepository()
    private val onboardingRepository = FakeOnboardingRepository()
    private val themeRepository = FakeThemeRepository()
    private val errorTracker = FakeErrorTracker()

    private fun viewModel() = MainViewModel(
        logger = logger,
        authService = authService,
        catalogRepository = catalogRepository,
        onboardingRepository = onboardingRepository,
        themeRepository = themeRepository,
        errorTracker = errorTracker,
    )

    /**
     * The tour outranks the session, so every case about the session starts past it. A test
     * that forgets this asserts `Onboarding` and reads as a session bug.
     */
    @Before
    fun onboardingIsDone() {
        onboardingRepository.seen.value = true
    }

    @Test
    fun `a stored session reads as signed in`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals(SessionState.SignedIn, viewModel().sessionState.value)
    }

    @Test
    fun `no stored session reads as signed out`() = runTest {
        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
    }

    @Test
    fun `signing out later switches the session back`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()

        authService.logout()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `signing in later switches the session forward`() = runTest {
        // The other direction of `signing out later switches the session back`: nothing but the
        // session changing moves the app between the auth flow and the tabs, so both ways have to
        // be asserted or half the switch is untested.
        val viewModel = viewModel()
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)

        authService.login("ada@example.com")

        assertEquals(SessionState.SignedIn, viewModel.sessionState.value)
        assertEquals(listOf(null, FakeAuthService.SESSION_ID), errorTracker.users)
    }

    @Test
    fun `a session that becomes unreadable mid-flight falls back to signed out`() = runTest {
        // `observeSession()` has already retried by the time a failure reaches here, and there is
        // no screen to put a dialog over — so the flow drops to the one state the user can act
        // from, and says so in the log rather than in their face.
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()
        assertEquals(SessionState.SignedIn, viewModel.sessionState.value)

        authService.sessionError = UnexpectedError(message = "disk gone")
        authService.session.value = null
        advanceUntilIdle()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `an unreadable session is signed out, with a warning rather than a dialog`() = runTest {
        authService.sessionError = UnexpectedError(message = "disk gone")

        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `an unfinished tour is the onboarding flow`() = runTest {
        onboardingRepository.seen.value = false

        assertEquals(SessionState.Onboarding, viewModel().sessionState.value)
    }

    @Test
    fun `an unfinished tour outranks a stored session`() = runTest {
        // A stored session on a device that has not seen the tour means a reinstall over one,
        // not that the tour was taken.
        onboardingRepository.seen.value = false
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals(SessionState.Onboarding, viewModel().sessionState.value)
    }

    @Test
    fun `finishing the tour moves the app on without the screen navigating`() = runTest {
        onboardingRepository.seen.value = false
        val viewModel = viewModel()

        onboardingRepository.markSeen()
        advanceUntilIdle()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `an unreadable onboarding flag skips the tour rather than repeating it`() = runTest {
        onboardingRepository.failWith = UnexpectedError(message = "disk gone")

        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `the session is unknown until it has been read`() = runTest {
        // Overrides the rule's UnconfinedTestDispatcher for this one case: under that dispatcher
        // the collections started in `init` have already produced a value by the time the
        // constructor returns, which is exactly what hides the state under test. Unknown holds
        // until *both* the session and the first-run flag have answered.
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        assertEquals(SessionState.Unknown, viewModel.sessionState.value)

        advanceUntilIdle()
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `the stored theme is what the app draws in`() = runTest {
        themeRepository.theme.value = ThemePreference.Dark

        assertEquals(ThemePreference.Dark, viewModel().theme.value)
    }

    @Test
    fun `changing the theme reaches the root`() = runTest {
        val viewModel = viewModel()

        themeRepository.setTheme(ThemePreference.Light)
        advanceUntilIdle()

        assertEquals(ThemePreference.Light, viewModel.theme.value)
    }

    @Test
    fun `the theme is null until it has been read, so the splash holds`() = runTest {
        // Same dispatcher swap as the session case above, and for the same reason.
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        assertNull(viewModel.theme.value)

        advanceUntilIdle()
        assertEquals(ThemePreference.System, viewModel.theme.value)
    }

    @Test
    fun `an unreadable theme falls back to the default rather than holding the splash`() = runTest {
        themeRepository.failWith = UnexpectedError(message = "disk gone")

        assertEquals(ThemePreference.DEFAULT, viewModel().theme.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `a cold-start deep link synthesises the path to the product`() = runTest {
        val viewModel = viewModel()

        viewModel.onDeepLink("app://product/coffee", coldStart = true)
        advanceUntilIdle()

        // Home, Categories, that product's category, then the product — so Up walks back
        // through the app rather than closing it.
        assertEquals(
            listOf(
                HomeDestination,
                CategoriesDestination,
                ProductsDestination(categoryId = "beverages", categoryName = "Beverages"),
                ProductDetailDestination(productId = "coffee"),
            ),
            viewModel.deepLink.value,
        )
    }

    @Test
    fun `a warm-start deep link pushes only the product`() = runTest {
        val viewModel = viewModel()

        viewModel.onDeepLink("app://product/coffee", coldStart = false)
        advanceUntilIdle()

        // The user's place in the stack is theirs; the product goes on top of it.
        assertEquals(listOf(ProductDetailDestination(productId = "coffee")), viewModel.deepLink.value)
    }

    @Test
    fun `a product not in the cache still opens, without a list it cannot name`() = runTest {
        val viewModel = viewModel()

        viewModel.onDeepLink("app://product/unknown", coldStart = true)
        advanceUntilIdle()

        assertEquals(
            listOf(HomeDestination, CategoriesDestination, ProductDetailDestination("unknown")),
            viewModel.deepLink.value,
        )
    }

    @Test
    fun `a link that is not one of ours leaves the stack alone`() = runTest {
        val viewModel = viewModel()

        viewModel.onDeepLink("app://order/1", coldStart = true)
        advanceUntilIdle()

        assertTrue(viewModel.deepLink.value.isEmpty())
    }

    @Test
    fun `an applied deep link is not applied twice`() = runTest {
        val viewModel = viewModel()
        viewModel.onDeepLink("app://product/coffee", coldStart = false)
        advanceUntilIdle()

        viewModel.onDeepLinkApplied()

        assertTrue(viewModel.deepLink.value.isEmpty())
    }

    @Test
    fun `a signed-in session is reported to the crash tracker by id`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        viewModel()

        // The id, never the address: a crash report is not the place for one.
        assertEquals(listOf("session-1"), errorTracker.users)
    }

    @Test
    fun `signing out clears the reported user`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()

        authService.session.value = null
        advanceUntilIdle()

        // Null on the way out, or the next person's reports are attributed to the last one.
        assertEquals(listOf("session-1", null), errorTracker.users)
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `an unreadable session clears the reported user too`() = runTest {
        authService.failWith = UnexpectedError()

        viewModel()

        assertEquals(listOf(null), errorTracker.users)
    }
}
