package com.example.androidproject1.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The tag every one of these tests reaches the component under test by. */
internal const val TAG = "component_underTest"

/**
 * What every component test is: the component composed inside [AppTheme], found by `testTag`, and
 * asserted on what a tap does rather than on how it looks.
 *
 * Screenshots are a different question and a different tool (`ui.2`). These say the things a
 * picture cannot: that a disabled button emits nothing, that a stepper stops at its floor, that a
 * field in error is never red and silent.
 *
 * A caller's `modifier` goes to the outermost element, so `Modifier.testTag(TAG)` on a compound
 * component tags the whole group. Reaching one part of it is
 * `hasClickAction() and hasAnyAncestor(hasTestTag(TAG))`.
 *
 * **Nothing is ever reached by text.** A component draws whatever string it is handed, so a finder
 * built from one says nothing about the component and everything about the test's own fixture.
 * Every tap and every state assertion goes through the tag, or — for the parts a component draws
 * itself, which a caller has no tag to put on — through position within it, or within the
 * `isPopup()` / `isDialog()` window it opens. Text appears only on the right-hand side of an
 * assertion, where the content *is* what is under test: that a badge of zero draws nothing, that
 * an error message replaces the helper rather than joining it, that the destructive verb is on the
 * button wired to confirm.
 */
@RunWith(RobolectricTestRunner::class)
abstract class ComponentTest {

    @get:Rule
    val compose = createComposeRule()

    /** Composes [content] inside the theme, which is where every component reads its roles from. */
    protected fun themed(content: @Composable () -> Unit) {
        compose.setContent { AppTheme { content() } }
    }
}
