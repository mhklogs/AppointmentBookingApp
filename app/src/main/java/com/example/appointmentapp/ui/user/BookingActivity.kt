package com.example.appointmentapp.ui.user

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.data.repository.FirestoreRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = AuthRepository()
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val serviceId = intent.getStringExtra("serviceId") ?: return
        val serviceName = intent.getStringExtra("serviceName") ?: ""

        findViewById<TextView>(R.id.tvServiceName).text = "Book: $serviceName"

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val rgTimeSlots = findViewById<RadioGroup>(R.id.rgTimeSlots)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(calendar.time)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = sdf.format(calendar.time)
        }

        // Generate Time Slots (Simplified)
        val times = listOf("09:00", "10:00", "11:00", "14:00", "15:00", "16:00")
        times.forEach { time ->
            val rb = RadioButton(this)
            rb.text = time
            rb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedTime = time
            }
            rgTimeSlots.addView(rb)
        }

        btnConfirm.setOnClickListener {
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Select time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val uid = auth.getCurrentUid() ?: return@setOnClickListener
            val email = auth.getCurrentEmail() ?: ""
            
            val apt = Appointment(
                userId = uid,
                userEmail = email,
                serviceId = serviceId,
                serviceName = serviceName,
                date = selectedDate,
                timeSlot = selectedTime
            )

            lifecycleScope.launch {
                val result = repository.bookAppointment(apt)
                if (result.isSuccess) {
                    Toast.makeText(this@BookingActivity, "Booked Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@BookingActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
