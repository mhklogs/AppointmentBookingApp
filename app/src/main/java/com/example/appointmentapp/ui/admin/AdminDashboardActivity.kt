package com.example.appointmentapp.ui.admin

import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Service
import com.example.appointmentapp.data.repository.FirestoreRepository
import com.example.appointmentapp.ui.user.AppointmentAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val adapter = AppointmentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val rv = findViewById<RecyclerView>(R.id.rvAppointments)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddService).setOnClickListener {
            showAddServiceDialog()
        }

        loadAppointments()
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val result = repository.getAllAppointments()
            result.onSuccess { adapter.updateList(it) }
                .onFailure {
                    Toast.makeText(this@AdminDashboardActivity, "Could not load appointments. Check your connection.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showAddServiceDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val pad = (16 * resources.displayMetrics.density).toInt()
        layout.setPadding(pad, pad, pad, pad)

        val etName = EditText(this).apply { hint = "Service Name" }
        val etPrice = EditText(this).apply { hint = "Price" }
        val etDuration = EditText(this).apply { hint = "Duration (mins)" }

        layout.addView(etName)
        layout.addView(etPrice)
        layout.addView(etDuration)

        AlertDialog.Builder(this)
            .setTitle("Add Service")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val duration = etDuration.text.toString().toIntOrNull() ?: 30
                
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.addService(Service(name = name, price = price, durationMinutes = duration))
                        Toast.makeText(this@AdminDashboardActivity, "Service Added", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
