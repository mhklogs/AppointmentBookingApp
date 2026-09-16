package com.example.appointmentapp.ui.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = AuthRepository()
    private val adapter = AppointmentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val uid = auth.getCurrentUid()
        if (uid != null) {
            lifecycleScope.launch {
                val result = repository.getUserAppointments(uid)
                result.onSuccess { adapter.updateList(it) }
                    .onFailure { Toast.makeText(this@HistoryActivity, "Could not load bookings. Check your connection.", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
