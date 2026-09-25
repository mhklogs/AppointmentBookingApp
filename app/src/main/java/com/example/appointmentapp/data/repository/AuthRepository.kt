package com.example.appointmentapp.data.repository

import com.example.appointmentapp.data.model.Role
import com.example.appointmentapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val uid = auth.currentUser?.uid ?: throw Exception("UID null")
            val snapshot = db.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String, role: Role, name: String = "", phone: String = ""): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("UID null")
            
            val user = User(uid = uid, email = email, name = name, phone = phone, role = role)
            
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserRole(): Role? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)?.role
        } catch (e: Exception) {
            null
        }
    }
    
    fun isUserLoggedIn() = auth.currentUser != null
    fun logout() = auth.signOut()
    fun getCurrentUid() = auth.currentUser?.uid
    fun getCurrentEmail() = auth.currentUser?.email
}
