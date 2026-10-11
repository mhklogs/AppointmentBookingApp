package com.example.appointmentapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Role
import com.example.appointmentapp.data.repository.AuthRepository
import com.example.appointmentapp.ui.admin.AdminDashboardActivity
import com.example.appointmentapp.ui.user.UserHomeActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val repository = AuthRepository()
    private var isRegister = false

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
        val tilName = findViewById<TextInputLayout>(R.id.tilName)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tvTitle = findViewById<TextView>(R.id.tvLoginTitle)
        val tvSubtitle = findViewById<TextView>(R.id.tvLoginSubtitle)

        fun render() {
            val extra = if (isRegister) View.VISIBLE else View.GONE
            tilName.visibility = extra
            tilPhone.visibility = extra
            rgRole.visibility = extra
            btnLogin.text = getString(if (isRegister) R.string.register else R.string.login)
            btnRegister.text = getString(if (isRegister) R.string.back_to_sign_in else R.string.register)
            tvTitle.text = getString(if (isRegister) R.string.create_account_title else R.string.welcome_back)
            tvSubtitle.text = getString(if (isRegister) R.string.create_account_subtitle else R.string.sign_in_continue)
        }

        fun onCreateAccount() {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val name = findViewById<EditText>(R.id.etName).text.toString()
            val phone = findViewById<EditText>(R.id.etPhone).text.toString()
            val role = if (rgRole.checkedRadioButtonId == R.id.rbAdmin) Role.ADMIN else Role.USER

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return
            }
            lifecycleScope.launch {
                val result = repository.register(email, pass, role, name, phone)
                if (result.isSuccess) {
                    checkRoleAndNavigate()
                } else {
                    Toast.makeText(this@LoginActivity, "Register Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        render()

        btnLogin.setOnClickListener {
            if (isRegister) {
                onCreateAccount()
                return@setOnClickListener
            }
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
            isRegister = !isRegister
            render()
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
