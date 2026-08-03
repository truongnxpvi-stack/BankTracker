package com.banktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType { DEBIT, CREDIT, UNKNOWN }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bank: String,
    val amount: Long,
    val type: TransactionType,
    val description: String,
    val category: String = "Khác",
    val balance: Long? = null,
    val rawMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
)
