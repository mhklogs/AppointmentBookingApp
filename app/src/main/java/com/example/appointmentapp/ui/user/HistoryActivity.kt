package com.example.appointmentapp.ui.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.AppointmentStatus
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.data.repository.FirestoreRepository
import com.example.appointmentapp.util.ReminderScheduler
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = AuthRepository()
    private val adapter = AppointmentAdapter(onClick = { showAppointmentActions(it) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val uid = auth.getCurrentUid()
        if (uid != null) {
            load()
        }
    }

    private fun load() {
        val uid = auth.getCurrentUid() ?: return
        lifecycleScope.launch {
            val result = repository.getUserAppointments(uid)
            result.onSuccess { adapter.updateList(it) }
                .onFailure { Toast.makeText(this@HistoryActivity, "Could not load bookings. Check your connection.", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showAppointmentActions(apt: Appointment) {
        val actions = mutableListOf<String>()
        if (apt.status == AppointmentStatus.BOOKED.name) actions.add("Cancel appointment")
        if (apt.status == AppointmentStatus.BOOKED.name || apt.status == AppointmentStatus.CONFIRMED.name) actions.add("Mark as arrived / check in")
        if (apt.status == AppointmentStatus.CHECKED_IN.name) actions.add("Mark completed")
        if (apt.status == AppointmentStatus.COMPLETED.name) actions.add("Rate this appointment")

        if (actions.isEmpty()) {
            Toast.makeText(this, "This appointment is finished. No actions available.", Toast.LENGTH_SHORT).show()
            return
        }

        val options = actions.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle(apt.serviceName + " - " + apt.date)
            .setItems(options) { _, which ->
                when (actions[which]) {
                    "Cancel appointment" -> cancelAppointment(apt)
                    "Mark as arrived / check in" -> checkIn(apt)
                    "Mark completed" -> completeAppointment(apt)
                    "Rate this appointment" -> showRatingDialog(apt)
                }
            }
            .show()
    }

    private fun cancelAppointment(apt: Appointment) {
        lifecycleScope.launch {
            ReminderScheduler.cancel(apt, this@HistoryActivity)
            val result = repository.cancelAppointment(apt)
            if (result.isSuccess) {
                Toast.makeText(this@HistoryActivity, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                load()
            } else {
                Toast.makeText(this@HistoryActivity, "Could not cancel.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIn(apt: Appointment) {
        lifecycleScope.launch {
            val result = repository.updateStatus(apt.id, AppointmentStatus.CHECKED_IN)
            if (result.isSuccess) {
                Toast.makeText(this@HistoryActivity, "Checked in. Code: ${apt.checkInCode.ifBlank { "auto" }}", Toast.LENGTH_SHORT).show()
                load()
            } else {
                Toast.makeText(this@HistoryActivity, "Could not check in.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun completeAppointment(apt: Appointment) {
        lifecycleScope.launch {
            val result = repository.updateStatus(apt.id, AppointmentStatus.COMPLETED)
            if (result.isSuccess) {
                Toast.makeText(this@HistoryActivity, "Marked completed.", Toast.LENGTH_SHORT).show()
                load()
            } else {
                Toast.makeText(this@HistoryActivity, "Could not update.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRatingDialog(apt: Appointment) {
        val ratings = arrayOf("★ 1 (Poor)", "★ 2 (Fair)", "★ 3 (Good)", "★ 4 (Very Good)", "★ 5 (Excellent)")
        android.app.AlertDialog.Builder(this)
            .setTitle("Rate ${apt.serviceName}")
            .setItems(ratings) { _, which ->
                val rating = which + 1
                lifecycleScope.launch {
                    val updated = apt.copy(status = AppointmentStatus.COMPLETED.name, rating = rating)
                    val result = repository.updateStatus(updated)
                    if (result.isSuccess) {
                        Toast.makeText(this@HistoryActivity, "Thanks for rating $rating/5!", Toast.LENGTH_SHORT).show()
                        load()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}