package com.banktracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.banktracker.R
import com.banktracker.data.AppDatabase
import com.banktracker.data.Transaction
import com.banktracker.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView
    private val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private val dateFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.listHistory)
        tvEmpty  = findViewById(R.id.tvEmpty)

        supportActionBar?.title = "Lịch sử giao dịch"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadHistory()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadHistory() {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            db.transactionDao().getAll().observe(this) { list ->
                try {
                    if (list.isNullOrEmpty()) {
                        tvEmpty.visibility  = View.VISIBLE
                        listView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility  = View.GONE
                        listView.visibility = View.VISIBLE
                        listView.adapter = TransactionAdapter(list)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi hiển thị: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi database: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    inner class TransactionAdapter(private val items: List<Transaction>) : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(pos: Int) = items[pos]
        override fun getItemId(pos: Int) = items[pos].id

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)

            val tx = items[pos]
            val sign  = if (tx.type == TransactionType.DEBIT) "- " else "+ "
            val color = if (tx.type == TransactionType.DEBIT) 0xFFDC2626.toInt() else 0xFF16A34A.toInt()

            view.findViewById<TextView>(R.id.tvDate).text   = dateFmt.format(Date(tx.timestamp))
            view.findViewById<TextView>(R.id.tvBank).text   = tx.bank
            view.findViewById<TextView>(R.id.tvCat).text    = tx.category
            view.findViewById<TextView>(R.id.tvDesc).text   = tx.description.take(60)
            val tvAmt = view.findViewById<TextView>(R.id.tvAmount)
            tvAmt.text      = "$sign${fmt.format(tx.amount)} đ"
            tvAmt.setTextColor(color)

            return view
        }
    }
}
