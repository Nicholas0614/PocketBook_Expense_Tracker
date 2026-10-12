
package com.example.pocketbook_expensetracker.data.repo

interface AuthRepo {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun getUserName(): String
    fun isLoggedIn(): Boolean
    fun logout()

    companion object {
        fun getInstance(): AuthRepo = AuthRepoImpl()
    }
}
