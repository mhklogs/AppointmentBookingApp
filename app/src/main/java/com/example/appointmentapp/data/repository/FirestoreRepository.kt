package com.example.appointmentapp.data.repository

import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.AppointmentStatus
import com.example.appointmentapp.data.model.Service
import com.example.appointmentapp.data.model.WaitlistEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // Services (Admin)
    suspend fun addService(service: Service): Result<String> {
        return try {
            val ref = db.collection("services").document()
            val finalService = service.copy(id = ref.id)
            ref.set(finalService).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateService(service: Service): Result<Unit> {
        return try {
            db.collection("services").document(service.id).set(service).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServices(): Result<List<Service>> {
        return try {
            val snapshot = db.collection("services").get().await()
            Result.success(snapshot.toObjects(Service::class.java).filter { it.active })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllServices(): Result<List<Service>> {
        return try {
            val snapshot = db.collection("services").get().await()
            Result.success(snapshot.toObjects(Service::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Appointments (User)
    suspend fun bookAppointment(appointment: Appointment): Result<String> {
        return try {
            // Check for double booking
            val existing = db.collection("appointments")
                .whereEqualTo("date", appointment.date)
                .whereEqualTo("timeSlot", appointment.timeSlot)
                .whereEqualTo("serviceId", appointment.serviceId)
                .whereNotEqualTo("status", AppointmentStatus.CANCELLED.name)
                .get().await()

            if (!existing.isEmpty) {
                 return Result.failure(Exception("Time slot already booked"))
            }

            val ref = db.collection("appointments").document()
            val finalAppointment = appointment.copy(
                id = ref.id,
                checkInCode = generateCheckInCode()
            )
            ref.set(finalAppointment).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateCheckInCode(): String {
        return (100000..999999).random().toString()
    }

    suspend fun getUserAppointments(userId: String): Result<List<Appointment>> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayAppointments(): Result<List<Appointment>> {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val snapshot = db.collection("appointments")
                .whereEqualTo("date", today)
                .whereNotEqualTo("status", AppointmentStatus.CANCELLED.name)
                .get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return try {
            val snapshot = db.collection("appointments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("status", status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatus(appointment: Appointment): Result<Unit> {
        return try {
            db.collection("appointments").document(appointment.id).set(appointment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(appointment: Appointment): Result<Unit> {
        return updateStatus(appointment.copy(status = AppointmentStatus.CANCELLED.name))
    }

    // Available slots for a service on a date
    suspend fun getBookedSlots(serviceId: String, date: String): Result<List<String>> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("serviceId", serviceId)
                .whereEqualTo("date", date)
                .whereNotEqualTo("status", AppointmentStatus.CANCELLED.name)
                .get().await()
            val booked = snapshot.toObjects(Appointment::class.java).map { it.timeSlot }
            Result.success(booked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Waitlist
    suspend fun joinWaitlist(entry: WaitlistEntry): Result<String> {
        return try {
            val ref = db.collection("waitlist").document()
            ref.set(entry.copy(id = ref.id)).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWaitlist(): Result<List<WaitlistEntry>> {
        return try {
            val snapshot = db.collection("waitlist")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(WaitlistEntry::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeWaitlistEntry(id: String): Result<Unit> {
        return try {
            db.collection("waitlist").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}