package com.example.appointmentapp.data.model

import com.google.firebase.Timestamp

enum class Role {
    USER, ADMIN
}

data class User(
    val uid: String = "",
    val email: String = "",
    val role: Role = Role.USER
)

data class Service(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val durationMinutes: Int = 30
)

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val date: String = "", // YYYY-MM-DD
    val timeSlot: String = "", // HH:mm
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "BOOKED"
)
