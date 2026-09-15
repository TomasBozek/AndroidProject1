package com.example.androidproject1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.presentation.categories.CategoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import com.example.androidproject1.feature.catalog.presentation.products.ProductsDestination
import com.example.androidproject1.feature.home.presentation.home.HomeDestination
import com.example.androidproject1.feature.onboarding.domain.OnboardingRepository
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.ThemeRepository
import com.example.androidproject1.service.core.domain.ErrorTracker
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.network.ConnectivityMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single owner of the session, the first-run flag and the stored theme, and the only thing that
 * switches between the app's three flows.
 *
 * A plain [ViewModel] rather than a `BaseViewModel`, because it is not a screen: there is no state
 * to render, no user event to receive, and nothing to put a loading overlay or an error dialog
 * over. Screens change what is stored and let this react — see `SettingsViewModel.logout()` and
 * `OnboardingViewModel.finish()`.
 */
class MainViewModel(
    logger: Logger,
    private val authService: AuthService,
    private val catalogRepository: CatalogRepository,
    private val onboardingRepository: OnboardingRepository,
    private val themeRepository: ThemeRepository,
    private val errorTracker: ErrorTracker,
    connectivity: ConnectivityMonitor,
) : ViewModel() {

    private val logger = logger.withTag(TAG)

    // `null` means "not read yet", which is what makes the combination below say Unknown until
    // both stores have answered. Two flows rather than one combine over the sources themselves,
    // so a re-emission of the flag does not re-report the user to the crash tracker.
    private val signedIn = MutableStateFlow<Boolean?>(null)
    private val onboardingSeen = MutableStateFlow<Boolean?>(null)

    /** [SessionState.Unknown] until both the stored session and the first-run flag have been read. */
    val sessionState: StateFlow<SessionState> =
        combine(signedIn, onboardingSeen, ::flowFor)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Unknown)

    private val mutableDeepLink = MutableStateFlow<List<NavKey>>(emptyList())

    /**
     * The keys a link asked for, waiting to be applied to the back stack.
     *
     * Empty when there is nothing pending. `MainActivity` applies it and calls
     * [onDeepLinkApplied] — a one-shot handed over as state rather than as an event, because a
     * link that arrives before the flow is known has to survive until there is a stack to put
     * it on.
     */
    val deepLink: StateFlow<List<NavKey>> = mutableDeepLink.asStateFlow()

    private val mutableTheme = MutableStateFlow<ThemePreference?>(null)

    /**
     * The palette the whole app draws in, `null` until the stored choice has been read once.
     *
     * Nullable for the same reason [SessionState.Unknown] exists: a default applied before the
     * read lands is a light frame in front of someone who chose dark. The splash holds until
     * this is known, so nothing is drawn in the wrong palette and then swapped.
     */
    val theme: StateFlow<ThemePreference?> = mutableTheme.asStateFlow()

    /**
     * Whether the device has a route to the outside world, for the banner `MainActivity` draws
     * above every screen (D68). The monitor `:app` binds per flavor owns it; it is exposed here so
     * the activity has one view model to ask.
     */
    val online: StateFlow<Boolean> = connectivity.online

    init {
        observeSession()
        observeOnboarding()
        observeTheme()
    }

    // A member function, not an `init` body: a constructor parameter shadows the property of the
    // same name inside `init`, so `logger` there would be the untagged one.
    /**
     * Turns an incoming link into the keys it opens onto.
     *
     * @param coldStart whether the app was launched by this link. On a cold start the whole
     * path is synthesised — Home, Categories, that product's category, then the product — so
     * Up walks back through the app instead of closing it. On a warm one only the product is
     * pushed, onto whatever tab the user was already on.
     */
    fun onDeepLink(uri: String?, coldStart: Boolean) {
        val link = DeepLinks.parse(uri) ?: return
        viewModelScope.launch {
            mutableDeepLink.value = when (link) {
                is DeepLink.Product -> productKeys(link.productId, coldStart)
            }
        }
    }

    /** Called once the keys are on the back stack, so a rotation does not apply them again. */
    fun onDeepLinkApplied() {
        mutableDeepLink.value = emptyList()
    }

    private suspend fun productKeys(productId: String, coldStart: Boolean): List<NavKey> {
        val target = ProductDetailDestination(productId = productId)
        if (!coldStart) return listOf(target)

        // The link names a product, not a path, so the path is looked up. A product that is not
        // in the cache still opens — its own screen loads it — but Up then goes to Categories
        // rather than to a list that cannot be named.
        val product = (catalogRepository.getProduct(productId) as? Outcome.Success)?.data
        val category = product?.let { found ->
            (catalogRepository.observeCategories().first() as? Outcome.Success)
                ?.data
                ?.firstOrNull { it.id == found.categoryId }
        }

        return listOfNotNull(
            HomeDestination,
            CategoriesDestination,
            category?.let { ProductsDestination(categoryId = it.id, categoryName = it.name) },
            target,
        )
    }

    private fun observeSession() {
        viewModelScope.launch {
            authService.observeSession().collect { outcome ->
                signedIn.value = when (outcome) {
                    is Outcome.Success -> {
                        // The only place that knows who is signed in, so the only place that can
                        // tell the tracker. The id is opaque — never the address, because a crash
                        // report is not the place for one — and null on sign-out, or the next
                        // person's reports are attributed to the last one.
                        errorTracker.setUser(outcome.data?.id)
                        outcome.data != null
                    }

                    // `observeSession()` has already retried, and there is no screen to put a
                    // dialog over. Signed out is the safe reading and the one flow from which the
                    // user can do something about it.
                    is Outcome.Failure -> {
                        logger.w { "Session unreadable (${outcome.error}); treating as signed out" }
                        errorTracker.setUser(null)
                        false
                    }
                }
            }
        }
    }

    private fun observeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.observeSeen().collect { outcome ->
                onboardingSeen.value = when (outcome) {
                    is Outcome.Success -> outcome.data

                    // Seen, on an unreadable flag: showing the tour to someone who has already
                    // taken it every time the disk hiccups is worse than skipping it once.
                    is Outcome.Failure -> {
                        logger.w { "Onboarding flag unreadable (${outcome.error}); skipping it" }
                        true
                    }
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeRepository.observeTheme().collect { outcome ->
                mutableTheme.value = when (outcome) {
                    is Outcome.Success -> outcome.data

                    // The repository has already retried. A palette is not worth holding the
                    // splash screen over, so the default is applied and the app starts.
                    is Outcome.Failure -> {
                        logger.w { "Theme unreadable (${outcome.error}); using the default" }
                        ThemePreference.DEFAULT
                    }
                }
            }
        }
    }

    private companion object {

        const val TAG = "MainViewModel"

        /**
         * The flow both stored facts add up to.
         *
         * The tour wins over the session: a stored session on a device that has not seen the tour
         * means the app was reinstalled over one, not that the tour was taken.
         */
        fun flowFor(signedIn: Boolean?, onboardingSeen: Boolean?): SessionState = when {
            signedIn == null || onboardingSeen == null -> SessionState.Unknown
            !onboardingSeen -> SessionState.Onboarding
            signedIn -> SessionState.SignedIn
            else -> SessionState.SignedOut
        }
    }
}
