package com.example.pocketbook_expensetracker.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Private on-device database. No Firebase account or internet required. */
class PocketBookDatabase(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "pocketbook_v2.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE transactions (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL CHECK(amount > 0),
                type TEXT NOT NULL CHECK(type IN ('Income','Expense')),
                payment_method TEXT NOT NULL,
                date TEXT NOT NULL,
                icon TEXT NOT NULL,
                note TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX transaction_date_idx ON transactions(date)")
        db.execSQL("""
            CREATE TABLE wallets (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL UNIQUE,
                kind TEXT NOT NULL,
                opening_balance REAL NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE budgets (
                id TEXT PRIMARY KEY NOT NULL,
                category TEXT NOT NULL,
                month TEXT NOT NULL,
                limit_amount REAL NOT NULL CHECK(limit_amount > 0),
                UNIQUE(category, month)
            )
        """.trimIndent())
        db.execSQL("INSERT INTO wallets(id,name,kind,opening_balance) VALUES('cash','Cash','Cash',0)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // A future schema change must use additive migrations; never drop user finance data.
        error("No database migration defined from version $oldVersion to $newVersion")
    }
}
