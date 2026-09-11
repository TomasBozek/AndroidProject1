package com.example.androidproject1.feature.trips.presentation.tripwizard

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

@Immutable
data class TripWizardState(
    val step: Int = STEP_DETAILS,
    val name: String = "",
    val type: TripType = TripType.Leisure,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val travelers: Int = 1,
    val advancedExpanded: Boolean = false,
    val notes: String = "",
    val destinationId: String? = null,
    val destinationName: String? = null,
    val budgetRange: ClosedFloatingPointRange<Float> = DEFAULT_BUDGET_RANGE,
) {

    /** Enough has been typed that leaving without asking would lose something. */
    val isDirty: Boolean
        get() = name.isNotBlank() || destinationId != null || notes.isNotBlank()

    val canContinue: Boolean
        get() = when (step) {
            STEP_DETAILS -> name.isNotBlank() &&
                startDate != null &&
                endDate != null &&
                !endDate.isBefore(startDate)

            STEP_DESTINATION -> destinationId != null
            else -> true
        }

    companion object {

        const val STEP_DETAILS = 0
        const val STEP_DESTINATION = 1
        const val STEP_REVIEW = 2
        const val STEP_COUNT = 3

        val DEFAULT_BUDGET_RANGE = 0.2f..0.5f

        const val BUDGET_MIN_MINOR = 0L
        const val BUDGET_MAX_MINOR = 20_000_00L

        /** Where a slider fraction sits between the two ends of the budget scale, in minor units. */
        fun budgetMinorFor(fraction: Float): Long =
            (BUDGET_MIN_MINOR + fraction * (BUDGET_MAX_MINOR - BUDGET_MIN_MINOR)).toLong()

        val PREVIEW = TripWizardState(
            step = STEP_DETAILS,
            name = "Summer in Lisbon",
            type = TripType.Leisure,
            startDate = LocalDate.of(2026, 7, 10),
            endDate = LocalDate.of(2026, 7, 17),
            travelers = 2,
        )
    }
}
