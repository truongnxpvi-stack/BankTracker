package com.banktracker.ui

import android.os.Bundle
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.banktracker.R
import com.banktracker.data.AppDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val dateFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val db = AppDatabase.getInstance(this)
        db.transactionDao().getAll().observe(this) { list ->
            val items = list.map { tx ->
                val sign = if (tx.type.name == "DEBIT") "- " else "+ "
                "${dateFmt.format(Date(tx.timestamp))}  ${tx.bank}\n${sign}${fmt.format(tx.amount)} đ  |  ${tx.category}\n${tx.description.take(60)}"
            }
            findViewById<ListView>(R.id.listHistory).adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        }
    }
}
