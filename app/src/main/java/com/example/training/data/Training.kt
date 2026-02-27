package com.example.training.data

/**
 * Data model for a saved training session.
 *
 * @param name training name (e.g. "Corsa", "Pugilato", or a user scheda name)
 * @param durationMillis total elapsed time in milliseconds
 * @param dateFormatted date string formatted as dd/MM/yyyy
 * @param distanceFormatted optional distance (string using comma as decimal separator, e.g. "12,450")
 * @param avgKmPerMin optional average speed in km per minute (nullable for non-run)
 */
data class Training(
    val name: String,
    val durationMillis: Long,
    val dateFormatted: String,
    val distanceFormatted: String? = null,
    val avgKmPerMin: Double? = null
)