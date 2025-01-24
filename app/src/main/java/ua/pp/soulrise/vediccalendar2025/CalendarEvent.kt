package ua.pp.soulrise.vediccalendar2025

import java.time.LocalDate

data class CalendarEvent(
    val date: LocalDate,
    val description: String,
    val imageResourceName: String? = null // Имя файла изображения в assets/images/
) 