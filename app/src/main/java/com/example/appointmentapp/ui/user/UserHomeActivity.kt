package com.example.appointmentapp.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class UserHomeActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val adapter = ServiceAdapter { service ->
        val intent = Intent(this, BookingActivity::class.java)
        intent.putExtra("serviceId", service.id)
        intent.putExtra("serviceName", service.name)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        val rv = findViewById<RecyclerView>(R.id.rvServices)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        loadServices()
    }

    private fun loadServices() {
        lifecycleScope.launch {
            val result = repository.getServices()
            result.onSuccess { adapter.updateList(it) }
                .onFailure { Toast.makeText(this@UserHomeActivity, "Could not load services. Check your connection.", Toast.LENGTH_SHORT).show() }
        }
    }
}
