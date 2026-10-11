package com.example.appointmentapp.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.data.repository.FirestoreRepository
import com.example.appointmentapp.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class UserHomeActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = AuthRepository()
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

        auth.getCurrentEmail()?.let {
            findViewById<TextView>(R.id.tvUserSubtitle).text = getString(R.string.signed_in_as, it)
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            auth.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.tvEmptyTitle).text = getString(R.string.empty_services_title)
        findViewById<TextView>(R.id.tvEmptyBody).text = getString(R.string.empty_services_body)

        loadServices()
    }

    private fun loadServices() {
        lifecycleScope.launch {
            val result = repository.getServices()
            result.onSuccess {
                adapter.updateList(it)
                findViewById<View>(R.id.emptyState).visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
                .onFailure { Toast.makeText(this@UserHomeActivity, "Could not load services. Check your connection.", Toast.LENGTH_SHORT).show() }
        }
    }
}
