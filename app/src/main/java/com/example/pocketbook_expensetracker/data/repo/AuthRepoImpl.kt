package com.example.pocketbook_expensetracker.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepoImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepo {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User not found")
            val user = hashMapOf("name" to name, "email" to email, "createdAt" to System.currentTimeMillis())
            db.collection("users").document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getUserName(): String {
        val uid = auth.currentUser?.uid ?: return ""
        val document = db.collection("users").document(uid).get().await()
        return document.getString("name").orEmpty()
    }


    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun logout() {
        auth.signOut()
    }
}
