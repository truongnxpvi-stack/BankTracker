package com.banktracker.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.banktracker.R
import com.banktracker.data.AppDatabase
import com.banktracker.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listView = ListView(this)
        setContentView(listView)

        supportActionBar?.title = "Lịch sử giao dịch"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val dateFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

        try {
            val db = AppDatabase.getInstance(applicationContext)
            db.transactionDao().getAll().observe(this) { list ->
                try {
                    if (list.isNullOrEmpty()) {
                        val tv = TextView(this)
                        tv.text = "Chưa có giao dịch nào.\nHãy thực hiện giao dịch ngân hàng để xem tại đây."
                        tv.textSize = 15f
                        tv.setPadding(48, 80, 48, 0)
                        setContentView(tv)
                        return@observe
                    }

                    val items = list.map { tx ->
                        val sign = if (tx.type == TransactionType.DEBIT) "▼" else "▲"
                        val amt = fmt.format(tx.amount)
                        val date = dateFmt.format(Date(tx.timestamp))
                        "$sign $amt đ  |  ${tx.bank}  |  ${tx.category}\n${date}  —  ${tx.description.take(50)}"
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_2,
                        android.R.id.text1,
                        items
                    )
                    listView.adapter = adapter

                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi hiển thị: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi DB: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
