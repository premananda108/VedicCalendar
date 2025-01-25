package ua.pp.soulrise.vediccalendar

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.noties.markwon.Markwon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var vedicCalendar: VedicCalendar
    private var currentDate: LocalDate = LocalDate.now()
    private lateinit var tvDate: TextView
    private lateinit var tvEvent: TextView
    private lateinit var ivEventImage: ImageView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var markwon: Markwon
    
    // Константы для жестов
    private companion object {
        const val SWIPE_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }
    
    // Форматтер даты как статическое поле
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        initializeComponents()
        initGestureDetector()
        updateDisplay()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        //supportActionBar?.title = getString(R.string.app_name)
    }

    private fun initializeComponents() {
        vedicCalendar = VedicCalendar(this)
        markwon = Markwon.create(this)
        
        tvDate = findViewById(R.id.tvDate)
        tvEvent = findViewById(R.id.tvEvent)
        ivEventImage = findViewById(R.id.ivEventImage)
        tvEvent.movementMethod = LinkMovementMethod.getInstance()

        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.btnPrevious).setOnClickListener {
            navigateDate(-1)
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            navigateDate(1)
        }
    }

    private fun navigateDate(days: Int) {
        currentDate = currentDate.plusDays(days.toLong())
        updateDisplay()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(ev)) true else super.dispatchTouchEvent(ev)
    }

    private fun initGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = false

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
                    abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    
                    navigateDate(if (diffX > 0) -1 else 1)
                    return true
                }
                return false
            }
        })
    }

    private fun updateDisplay() {
        tvDate.text = currentDate.format(dateFormatter)
        
        val event = vedicCalendar.getEventForDate(currentDate)
        updateEventDisplay(event)
    }

    private fun updateEventDisplay(event: CalendarEvent?) {
        // Сбрасываем позицию прокрутки в начало
        tvEvent.scrollTo(0, 0)
        
        event?.description?.let { description ->
            markwon.setMarkdown(tvEvent, description)
        } ?: run {
            tvEvent.text = getString(R.string.no_event)
        }
        
        updateEventImage(event)
    }

    private fun updateEventImage(event: CalendarEvent?) {
        if (event != null) {
            val bitmap = vedicCalendar.loadEventImage(event)
            if (bitmap != null) {
                ivEventImage.apply {
                    alpha = 0f
                    setImageBitmap(bitmap)
                    visibility = View.VISIBLE
                    animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                }
            } else {
                ivEventImage.visibility = View.GONE
            }
        } else {
            ivEventImage.visibility = View.GONE
        }
    }
}