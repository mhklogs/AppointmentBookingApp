package com.example.appointmentapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Role
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.ui.admin.AdminDashboardActivity
import com.example.appointmentapp.ui.user.UserHomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (repository.isUserLoggedIn()) {
             checkRoleAndNavigate()
             return
        }
        
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    val result = repository.login(email, pass)
                    if (result.isSuccess) {
                        checkRoleAndNavigate()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val name = findViewById<EditText>(R.id.etName).text.toString()
            val phone = findViewById<EditText>(R.id.etPhone).text.toString()
            val role = if (rgRole.checkedRadioButtonId == R.id.rbAdmin) Role.ADMIN else Role.USER
            
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    val result = repository.register(email, pass, role, name, phone)
                    if (result.isSuccess) {
                        checkRoleAndNavigate()
                    } else {
                         Toast.makeText(this@LoginActivity, "Register Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkRoleAndNavigate() {
        lifecycleScope.launch {
            val role = repository.getCurrentUserRole()
            if (role == Role.ADMIN) {
                startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
            } else {
                startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
            }
            finish()
        }
    }
}
