
package com.example.pocketbook_expensetracker.data.repo

import com.example.pocketbook_expensetracker.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TransactionRepo {

    private fun transactionCollection() = db.collection("users")
        .document(auth.currentUser?.uid ?: throw Exception("User not logged in"))
        .collection("transactions")


    override fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val user = auth.currentUser

        if (user == null) {
            trySend(emptyList())
            close()
        } else {
            val registration = db.collection("users").document(user.uid)
                .collection("transactions").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            trySend(emptyList())
                        } else {
                            close(error)
                        }
                        return@addSnapshotListener
                    }

                    val transactions = snapshot?.documents?.mapNotNull { document ->
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    } ?: emptyList()

                    trySend(transactions)
                }

            awaitClose { registration.remove() }
        }
    }


    override suspend fun getTransactionById(id: String): Transaction? {
        val document = transactionCollection().document(id).get().await()
        return document.toObject(Transaction::class.java)?.copy(id = document.id)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionCollection().add(transaction.copy(id = "")).await()
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionCollection().document(transaction.id).set(transaction).await()
    }

    override suspend fun deleteTransaction(id: String) {
        transactionCollection().document(id).delete().await()
    }
}
