package com.example.vediccalendar2025

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VedicCalendar(private val context: Context) {
    private val events: List<CalendarEvent>

    init {
        events = loadEventsFromJson()
    }

    private fun loadEventsFromJson(): List<CalendarEvent> {
        val jsonString = context.assets.open("calendar_data.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val calendarData = gson.fromJson(jsonString, CalendarData::class.java)
        
        return calendarData.events.map { 
            CalendarEvent(
                LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE),
                it.description,
                it.image
            )
        }
    }

    fun getEventForDate(date: LocalDate): CalendarEvent? {
        return events.find { it.date == date }
    }

    private fun vectorDrawableToBitmap(drawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun loadEventImage(event: CalendarEvent): Bitmap? {
        return try {
            event.imageResourceName?.let { imageName ->
                // Сначала пробуем загрузить как векторное изображение из drawable
                val resourceId = context.resources.getIdentifier(
                    imageName.substringBeforeLast("."),
                    "drawable",
                    context.packageName
                )
                
                if (resourceId != 0) {
                    // Если нашли ресурс в drawable - загружаем как вектор
                    val drawable = ContextCompat.getDrawable(context, resourceId)
                    when (drawable) {
                        is VectorDrawable -> vectorDrawableToBitmap(drawable)
                        is BitmapDrawable -> drawable.bitmap
                        else -> null
                    }
                } else {
                    // Если не нашли - пробуем загрузить из assets как растровое
                    try {
                        context.assets.open("images/$imageName").use { 
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private data class JsonEvent(
        val date: String,
        val description: String,
        val image: String? = null
    )

    private data class CalendarData(
        val events: List<JsonEvent>
    )
} 