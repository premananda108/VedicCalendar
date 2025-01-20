package com.example.vediccalendar2025

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VedicCalendar(private val context: Context) {
    private val events: List<CalendarEvent>
    private val imageCache: LruCache<String, Bitmap>

    init {
        events = loadEventsFromJson()
        // Используем 1/8 доступной памяти для кэша
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        imageCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
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
                // Проверяем кэш
                imageCache.get(imageName) ?: run {
                    try {
                        context.assets.open("images/$imageName").use { 
                            BitmapFactory.decodeStream(it)?.also { bitmap ->
                                // Сохраняем в кэш
                                imageCache.put(imageName, bitmap)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("VedicCalendar", "Failed to load image from assets: $imageName", e)
                        null
                    }
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