package com.example.androidproject1.feature.trips.data.source

import com.example.androidproject1.feature.trips.domain.Destination

/**
 * What `DefaultLocalDestinationsDataSource` seeds the table with the first time it is read.
 *
 * A fixture rather than a network fetch: D20 wired the catalog to a Ktor `MockEngine`, and a
 * second HTTP round trip for a handful of rows that never change would only be a second copy of
 * that same seam. Trips are the user's own data and stay in the table `saveTrip` writes to; this
 * list never does.
 */
internal val DESTINATION_FIXTURES = listOf(
    Destination("lisbon", "Lisbon", "Portugal", "Hills, trams and the river Tagus."),
    Destination("prague", "Prague", "Czechia", "A skyline of spires over the Vltava."),
    Destination("kyoto", "Kyoto", "Japan", "Temples, gardens and a thousand gates."),
    Destination("reykjavik", "Reykjavik", "Iceland", "Glaciers, geysers and the northern lights."),
    Destination("marrakesh", "Marrakesh", "Morocco", "Souks, riads and the Atlas beyond."),
    Destination("wellington", "Wellington", "New Zealand", "A harbour city between two straits."),
    Destination("vancouver", "Vancouver", "Canada", "Mountains that meet the Pacific."),
    Destination("cape_town", "Cape Town", "South Africa", "Table Mountain over two oceans."),
)
