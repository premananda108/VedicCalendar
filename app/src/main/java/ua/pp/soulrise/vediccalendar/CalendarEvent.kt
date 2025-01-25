package ua.pp.soulrise.vediccalendar

import java.time.LocalDate

data class CalendarEvent(
    val date: LocalDate,
    val description: String,
    val imageResourceName: String? = null // Имя файла изображения в assets/images/
) 