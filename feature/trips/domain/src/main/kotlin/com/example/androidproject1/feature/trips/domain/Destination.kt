package com.example.androidproject1.feature.trips.domain

/** A place a trip can go. Fixture data today — see `DestinationFixtures` in the data layer. */
data class Destination(
    val id: String,
    val name: String,
    val country: String,
    val description: String,
)
