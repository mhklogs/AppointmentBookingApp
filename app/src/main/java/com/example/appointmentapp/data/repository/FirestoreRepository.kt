package com.example.appointmentapp.data.repository

import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // Services (Admin)
    suspend fun addService(service: Service) {
        val ref = db.collection("services").document()
        val finalService = service.copy(id = ref.id)
        ref.set(finalService).await()
    }

    suspend fun getServices(): Result<List<Service>> {
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
                .whereEqualTo("serviceId", appointment.serviceId) // Simplification: Prevent concurrent bookings for same service time
                .get().await()
                
            if (!existing.isEmpty) {
                 return Result.failure(Exception("Time slot already booked"))
            }

            val ref = db.collection("appointments").document()
            val finalAppointment = appointment.copy(id = ref.id)
            ref.set(finalAppointment).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserAppointments(userId: String): Result<List<Appointment>> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("userId", userId)
                .get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return try {
            val snapshot = db.collection("appointments").get().await()
            Result.success(snapshot.toObjects(Appointment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
