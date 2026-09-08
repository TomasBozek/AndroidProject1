package com.example.androidproject1.core.domain.result

import com.example.androidproject1.core.domain.error.UnexpectedError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val UNREACHABLE get() = Outcome.Failure(UnexpectedError(message = "Unhandled Outcome combination."))

/** Combines two outcome flows, short-circuiting on the first [Outcome.Failure]. */
fun <A, B> combineOutcomes(
    flowA: Flow<Outcome<A>>,
    flowB: Flow<Outcome<B>>,
): Flow<Outcome<Pair<A, B>>> =
    combine(flowA, flowB) { a, b ->
        when {
            a is Outcome.Failure -> a
            b is Outcome.Failure -> b
            a is Outcome.Success && b is Outcome.Success -> Outcome.Success(a.data to b.data)
            else -> UNREACHABLE
        }
    }

fun <A, B, C> combineOutcomes(
    flowA: Flow<Outcome<A>>,
    flowB: Flow<Outcome<B>>,
    flowC: Flow<Outcome<C>>,
): Flow<Outcome<Triple<A, B, C>>> =
    combine(flowA, flowB, flowC) { a, b, c ->
        when {
            a is Outcome.Failure -> a
            b is Outcome.Failure -> b
            c is Outcome.Failure -> c
            a is Outcome.Success && b is Outcome.Success && c is Outcome.Success ->
                Outcome.Success(Triple(a.data, b.data, c.data))
            else -> UNREACHABLE
        }
    }

/** Feeds the success value of [first] into [next], emitting both. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <A, B> chainOutcomes(
    first: Flow<Outcome<A>>,
    next: (A) -> Flow<Outcome<B>>,
): Flow<Outcome<Pair<A, B>>> =
    first.flatMapLatest { firstOutcome ->
        when (firstOutcome) {
            is Outcome.Success -> next(firstOutcome.data).map { secondOutcome ->
                when (secondOutcome) {
                    is Outcome.Success -> Outcome.Success(firstOutcome.data to secondOutcome.data)
                    is Outcome.Failure -> secondOutcome
                }
            }

            is Outcome.Failure -> flowOf(firstOutcome)
        }
    }
