package com.example.vediccalendar2025

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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

    fun loadEventImage(event: CalendarEvent): Bitmap? {
        return try {
            event.imageResourceName?.let { imageName ->
                Log.d("VedicCalendar", "Loading image: $imageName")
                try {
                    context.assets.open("images/$imageName").use { 
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    Log.e("VedicCalendar", "Failed to load image from assets: $imageName", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("VedicCalendar", "Error loading image", e)
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