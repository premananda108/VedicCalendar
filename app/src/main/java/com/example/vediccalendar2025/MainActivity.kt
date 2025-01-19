package com.example.vediccalendar2025

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import io.noties.markwon.Markwon
import android.text.method.LinkMovementMethod

class MainActivity : AppCompatActivity() {
    private lateinit var vedicCalendar: VedicCalendar
    private var currentDate: LocalDate = LocalDate.now()
    private lateinit var tvDate: TextView
    private lateinit var tvEvent: TextView
    private lateinit var ivEventImage: ImageView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var markwon: Markwon
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Настройка Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        vedicCalendar = VedicCalendar(this)
        markwon = Markwon.create(this)
        initViews()
        initGestureDetector()
        updateDisplay()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun initGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - (e1?.x ?: 0f)
                val diffY = e2.y - (e1?.y ?: 0f)

                if (abs(diffX) > abs(diffY) && 
                    abs(diffX) > SWIPE_THRESHOLD && 
                    abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    
                    if (diffX > 0) {
                        // Свайп вправо - предыдущий день
                        currentDate = currentDate.minusDays(1)
                    } else {
                        // Свайп влево - следующий день
                        currentDate = currentDate.plusDays(1)
                    }
                    updateDisplay()
                    return true
                }
                return false
            }
        })
    }

    private fun initViews() {
        tvDate = findViewById(R.id.tvDate)
        tvEvent = findViewById(R.id.tvEvent)
        ivEventImage = findViewById(R.id.ivEventImage)

        // Настраиваем поддержку кликабельных ссылок
        tvEvent.movementMethod = LinkMovementMethod.getInstance()

        findViewById<Button>(R.id.btnPrevious).setOnClickListener { 
            currentDate = currentDate.minusDays(1)
            updateDisplay()
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            currentDate = currentDate.plusDays(1)
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        val event = vedicCalendar.getEventForDate(currentDate)
        
        tvDate.text = currentDate.format(dateFormatter)
        event?.description?.let { description ->
            markwon.setMarkdown(tvEvent, description)
        } ?: run {
            tvEvent.text = "Нет события на эту дату"
        }
        
        // Загрузка и отображение изображения с анимацией
        if (event != null) {
            val bitmap = vedicCalendar.loadEventImage(event)
            if (bitmap != null) {
                ivEventImage.alpha = 0f
                ivEventImage.setImageBitmap(bitmap)
                ivEventImage.visibility = View.VISIBLE
                ivEventImage.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            } else {
                ivEventImage.visibility = View.GONE
            }
        } else {
            ivEventImage.visibility = View.GONE
        }
    }
}