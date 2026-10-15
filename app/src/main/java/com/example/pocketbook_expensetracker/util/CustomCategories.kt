
package com.example.pocketbook_expensetracker.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object CustomCategories {

    private val builtIn = setOf("Food", "Groceries", "Transport", "Bills", "Fun", "Health", "Shopping", "New")

    private fun prefs(context: Context) = context.getSharedPreferences(
        "pocketbook_categories_${FirebaseAuth.getInstance().currentUser?.uid ?: "guest"}",
        Context.MODE_PRIVATE
    )

    fun get(context: Context): List<String> {
        return prefs(context).getStringSet("categories", emptySet()).orEmpty().sorted()
    }

    fun add(context: Context, rawName: String): String? {
        val name = rawName.trim()
        if (name.isBlank() || name.length > 20 || (builtIn + get(context)).any { it.equals(name, true) }) return null
        prefs(context).edit().putStringSet("categories", (get(context) + name).toSet()).apply()
        return name
    }
}
