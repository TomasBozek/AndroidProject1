package com.example.androidproject1.core.ui

/**
 * Events raised by framework-level UI ([com.example.androidproject1.core.ui.component.Screen])
 * rather than by a feature's own screen — currently only alert dialog interactions.
 *
 * Each carries the [id] of the alert that produced it, so a ViewModel showing several different
 * dialogs can tell them apart.
 */
sealed interface CommonEvent : Event {

    sealed interface AlertDialogAction : CommonEvent {

        val id: String
        val data: Map<String, Any>

        data class PrimaryClicked(
            override val id: String,
            override val data: Map<String, Any> = emptyMap(),
        ) : AlertDialogAction

        data class SecondaryClicked(
            override val id: String,
            override val data: Map<String, Any> = emptyMap(),
        ) : AlertDialogAction

        data class Dismissed(
            override val id: String,
            override val data: Map<String, Any> = emptyMap(),
        ) : AlertDialogAction
    }
}
