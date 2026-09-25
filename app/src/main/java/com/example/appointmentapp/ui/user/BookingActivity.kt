package com.example.appointmentapp.ui.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.AppointmentStatus
import com.example.appointmentapp.data.model.Service
import com.example.appointmentapp.data.model.WaitlistEntry
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.data.repository.FirestoreRepository
import com.example.appointmentapp.util.ReminderScheduler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = AuthRepository()
    private var selectedDate = ""
    private var selectedTime = ""
    private var service: Service? = null
    private var bookedSlots = listOf<String>()
    private lateinit var tvConfirmation: TextView
    private var lastBtnConfirm: Button? = null

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val serviceId = intent.getStringExtra("serviceId") ?: return
        val serviceName = intent.getStringExtra("serviceName") ?: ""

        val tvServiceName = findViewById<TextView>(R.id.tvServiceName)
        tvServiceName.text = "Book: $serviceName"

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        tvConfirmation = findViewById(R.id.tvConfirmation)
        lastBtnConfirm = btnConfirm

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(calendar.time)

        // Booking window: next 30 days
        calendarView.minDate = System.currentTimeMillis() - 1000
        val maxCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        calendarView.maxDate = maxCal.timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = sdf.format(calendar.time)
            loadSlots()
        }

        val rgTimeSlots = findViewById<RadioGroup>(R.id.rgTimeSlots)
        loadService(serviceId, serviceName, rgTimeSlots, btnConfirm, calendarView)

        findViewById<Button>(R.id.btnWaitlist).setOnClickListener {
            if (bookedSlots.isEmpty()) {
                Toast.makeText(this, "No fully-booked slots to join. Pick any free slot!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showWaitlistDialog(bookedSlots, serviceName, serviceId)
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        btnConfirm.setOnClickListener { confirmBooking() }
    }

    private fun loadService(serviceId: String, serviceName: String, rg: RadioGroup, btn: Button, cal: CalendarView) {
        lifecycleScope.launch {
            val result = repository.getAllServices()
            service = result.getOrNull()?.firstOrNull { it.id == serviceId }
                ?: Service(id = serviceId, name = serviceName, price = 0.0, durationMinutes = 30)
            loadSlots()
        }
    }

    private fun loadSlots() {
        val svc = service ?: return
        val rgTimeSlots = findViewById<RadioGroup>(R.id.rgTimeSlots)
        lifecycleScope.launch {
            val bookedResult = repository.getBookedSlots(svc.id, selectedDate)
            bookedSlots = bookedResult.getOrElse { emptyList() }
            rgTimeSlots.removeAllViews()

            val times = generateSlots(svc)
            val now = Calendar.getInstance()
            computeSlotAvailability(now, rgTimeSlots, times)
        }
    }

    private fun computeSlotAvailability(now: Calendar, rg: RadioGroup, times: List<String>) {
        times.forEach { time ->
            val rb = RadioButton(this)
            rb.text = time
            val isPast = isPastTime(now, time)
            val isBooked = bookedSlots.contains(time)
            rb.isEnabled = !isPast && !isBooked
            if (rb.isEnabled) {
                rb.text = "$time  ✓"
            } else if (isBooked) {
                rb.text = "$time  (booked)"
            } else {
                rb.text = "$time  (passed)"
            }
            rb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedTime = time
            }
            rg.addView(rb)
        }
        if (times.isNotEmpty()) rg.check(-1)
    }

    private fun generateSlots(svc: Service): List<String> {
        // Business hours 09:00 - 18:00
        val startHour = 9
        val endHour = 18
        val step = (svc.durationMinutes + svc.bufferMinutes).coerceAtLeast(15)
        val slots = mutableListOf<String>()
        var minute = startHour * 60
        while (minute + svc.durationMinutes <= endHour * 60) {
            val h = minute / 60
            val m = minute % 60
            slots.add(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            minute += step
        }
        return slots
    }

    private fun isPastTime(now: Calendar, time: String): Boolean {
        val parts = time.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return selectedDate == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                && (h * 60 + m) <= nowMin
    }

    private fun showWaitlistDialog(slots: List<String>, serviceName: String, serviceId: String) {
        val options = slots.map { "$it (fully booked)" }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("Join Waitlist")
            .setSingleChoiceItems(options, 0) { _, which -> selectedTime = slots[which] }
            .setPositiveButton("Join") { _, _ ->
                val uid = auth.getCurrentUid() ?: return@setPositiveButton
                val email = auth.getCurrentEmail() ?: ""
                val entry = WaitlistEntry(
                    userId = uid,
                    userEmail = email,
                    serviceId = serviceId,
                    serviceName = serviceName,
                    date = selectedDate,
                    timeSlot = selectedTime
                )
                lifecycleScope.launch {
                    val result = repository.joinWaitlist(entry)
                    if (result.isSuccess) {
                        Toast.makeText(this@BookingActivity, "You're on the waitlist! We'll notify you if a spot frees up.", Toast.LENGTH_LONG).show()
                        tvConfirmation.text = "Waitlisted for $serviceName on $selectedDate at $selectedTime."
                    } else {
                        Toast.makeText(this@BookingActivity, "Could not join waitlist.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmBooking() {
        val svc = service ?: run {
            Toast.makeText(this, "Service not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Select time", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = auth.getCurrentUid() ?: return
        val email = auth.getCurrentEmail() ?: ""

        val apt = Appointment(
            userId = uid,
            userEmail = email,
            serviceId = svc.id,
            serviceName = svc.name,
            date = selectedDate,
            timeSlot = selectedTime,
            status = AppointmentStatus.BOOKED.name
        )

        lifecycleScope.launch {
            val result = repository.bookAppointment(apt)
            if (result.isSuccess) {
                ReminderScheduler.schedule(apt.copy(id = result.getOrNull() ?: ""), this@BookingActivity)
                tvConfirmation.text = "Booked ${svc.name} on ${apt.date} at ${apt.timeSlot}. A reminder will be sent 1h before."
                Toast.makeText(this@BookingActivity, "Booked Successfully!", Toast.LENGTH_SHORT).show()
                lastBtnConfirm?.isEnabled = false
            } else {
                Toast.makeText(this@BookingActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}