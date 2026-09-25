package com.example.appointmentapp.ui.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.AppointmentStatus
import com.example.appointmentapp.data.model.Service
import com.example.appointmentapp.data.model.WaitlistEntry
import com.example.appointmentapp.data.repository.FirestoreRepository
import com.example.appointmentapp.ui.user.AppointmentAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val adapter = AppointmentAdapter(onClick = { showStatusManagement(it) })
    private var appointments = listOf<Appointment>()
    private var filter = "ALL"

    private fun todayStr(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val rv = findViewById<RecyclerView>(R.id.rvAppointments)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<TextView>(R.id.tvToday).setOnClickListener {
            filter = "TODAY"
            applyFilter()
        }
        findViewById<TextView>(R.id.tvWaitlist).setOnClickListener {
            showWaitlist()
        }
        findViewById<TextView>(R.id.tvIncome).setOnClickListener {
            computeIncome()
        }
        findViewById<TextView>(R.id.tvAll).setOnClickListener {
            filter = "ALL"
            applyFilter()
        }

        findViewById<FloatingActionButton>(R.id.fabAddService).setOnClickListener {
            showAddServiceDialog()
        }

        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val result = repository.getAllAppointments()
            result.onSuccess {
                appointments = it
                applyFilter()
            }.onFailure {
                Toast.makeText(this@AdminDashboardActivity, "Could not load appointments. Check your connection.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilter() {
        val list = when (filter) {
            "TODAY" -> appointments.filter { it.date == todayStr() && it.status != AppointmentStatus.CANCELLED.name }
            "ALL" -> appointments
            else -> appointments
        }
        adapter.updateList(list)

        val completed = appointments.filter { it.status == AppointmentStatus.COMPLETED.name && !it.checkInCode.isBlank() }
        val todayCount = appointments.count { it.date == todayStr() && it.status != AppointmentStatus.CANCELLED.name }
        findViewById<TextView>(R.id.tvToday).text = "Today: $todayCount"
        findViewById<TextView>(R.id.tvIncome).text = "Income: $${completed.size * 50}"
    }

    private fun showWaitlist() {
        lifecycleScope.launch {
            val result = repository.getWaitlist()
            result.onSuccess { entries ->
                if (entries.isEmpty()) {
                    Toast.makeText(this@AdminDashboardActivity, "Waitlist is empty.", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                val lines = entries.map { "${it.serviceName} - ${it.date} ${it.timeSlot}\n  ${it.userEmail}" }.toTypedArray()
                AlertDialog.Builder(this@AdminDashboardActivity)
                    .setTitle("Waitlist (${entries.size})")
                    .setItems(lines, null)
                    .setPositiveButton("OK", null)
                    .show()
            }.onFailure {
                Toast.makeText(this@AdminDashboardActivity, "Could not load waitlist.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun computeIncome() {
        val completed = appointments.filter { it.status == AppointmentStatus.COMPLETED.name }
        if (completed.isEmpty()) {
            Toast.makeText(this, "No completed appointments yet.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val services = repository.getAllServices().getOrNull().orEmpty()
            val total = completed.sumOf { a -> services.firstOrNull { it.id == a.serviceId }?.price ?: 0.0 }
            Toast.makeText(this@AdminDashboardActivity, "Completed income: $${"%.2f".format(total)}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showStatusManagement(apt: Appointment) {
        if (apt.status == AppointmentStatus.CANCELLED.name || apt.status == AppointmentStatus.COMPLETED.name) {
            return
        }
        val options = when (apt.status) {
            AppointmentStatus.BOOKED.name -> arrayOf("Confirm", "Mark No-Show", "Cancel")
            AppointmentStatus.CONFIRMED.name -> arrayOf("Mark Checked-In", "Cancel")
            AppointmentStatus.CHECKED_IN.name -> arrayOf("Mark Completed", "Cancel")
            else -> arrayOf("Cancel")
        }
        AlertDialog.Builder(this)
            .setTitle(apt.serviceName + " - " + apt.userEmail)
            .setItems(options) { _, which ->
                val action = options[which]
                lifecycleScope.launch {
                    val result = when (action) {
                        "Confirm" -> repository.updateStatus(apt.id, AppointmentStatus.CONFIRMED)
                        "Mark Checked-In" -> repository.updateStatus(apt.id, AppointmentStatus.CHECKED_IN)
                        "Mark Completed" -> repository.updateStatus(apt.id, AppointmentStatus.COMPLETED)
                        "Mark No-Show" -> repository.updateStatus(apt.id, AppointmentStatus.NO_SHOW)
                        "Cancel" -> repository.updateStatus(apt.id, AppointmentStatus.CANCELLED)
                        else -> Result.success(Unit)
                    }
                    if (result.isSuccess) {
                        Toast.makeText(this@AdminDashboardActivity, "Updated: $action", Toast.LENGTH_SHORT).show()
                        load()
                    } else {
                        Toast.makeText(this@AdminDashboardActivity, "Could not update.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAddServiceDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val pad = (16 * resources.displayMetrics.density).toInt()
        layout.setPadding(pad, pad, pad, pad)

        val etName = EditText(this).apply { hint = "Service Name" }
        val etDescription = EditText(this).apply { hint = "Description" }
        val etPrice = EditText(this).apply { hint = "Price" }
        val etDuration = EditText(this).apply { hint = "Duration (mins)" }
        val etBuffer = EditText(this).apply { hint = "Buffer between bookings (mins)" }

        layout.addView(etName)
        layout.addView(etDescription)
        layout.addView(etPrice)
        layout.addView(etDuration)
        layout.addView(etBuffer)

        AlertDialog.Builder(this)
            .setTitle("Add Service")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val duration = etDuration.text.toString().toIntOrNull() ?: 30
                val buffer = etBuffer.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.addService(
                            Service(
                                name = name,
                                description = etDescription.text.toString(),
                                price = price,
                                durationMinutes = duration,
                                bufferMinutes = buffer
                            )
                        )
                        Toast.makeText(this@AdminDashboardActivity, "Service Added", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}