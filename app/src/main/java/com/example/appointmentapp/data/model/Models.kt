package com.example.appointmentapp.data.model

import com.google.firebase.Timestamp

enum class Role {
    USER, ADMIN
}

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: Role = Role.USER
)

data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val durationMinutes: Int = 30,
    val bufferMinutes: Int = 0,
    val active: Boolean = true
)

enum class AppointmentStatus {
    BOOKED, CONFIRMED, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW
}

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val date: String = "", // YYYY-MM-DD
    val timeSlot: String = "", // HH:mm
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = AppointmentStatus.BOOKED.name,
    val checkInCode: String = "",
    val notes: String = "",
    val rating: Int = 0,
    val review: String = ""
)

data class WaitlistEntry(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
