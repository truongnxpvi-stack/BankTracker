package com.banktracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.banktracker.R
import com.banktracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainContent: LinearLayout
    private lateinit var tvExpense: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvCount: TextView
    private lateinit var btnHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ view
        mainContent = findViewById(R.id.mainContent)
        tvExpense   = findViewById(R.id.tvExpense)
        tvIncome    = findViewById(R.id.tvIncome)
        tvCount     = findViewById(R.id.tvCount)
        btnHistory  = findViewById(R.id.btnHistory)

        // Ẩn nội dung trước khi xác thực
        mainContent.visibility = View.INVISIBLE

        // Xác thực vân tay
        authenticateUser()
    }

    private fun authenticateUser() {
        if (!BiometricHelper.canAuthenticate(this)) {
            onAuthSuccess()
            return
        }
        BiometricHelper.showPrompt(
            activity = this,
            onSuccess = { onAuthSuccess() },
            onFailed  = { },
            onError   = { msg ->
                Toast.makeText(this, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun onAuthSuccess() {
        mainContent.visibility = View.VISIBLE
        checkPermissions()
        loadData()
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun loadData() {
        val thisMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)

            db.transactionDao().getTotalExpenseByMonth(thisMonth)
                .observe(this@MainActivity) { total ->
                    tvExpense.text = "Chi tháng này: ${fmt.format(total ?: 0)} đ"
                }

            db.transactionDao().getTotalIncomeByMonth(thisMonth)
                .observe(this@MainActivity) { total ->
                    tvIncome.text = "Thu tháng này: ${fmt.format(total ?: 0)} đ"
                }

            db.transactionDao().getAll()
                .observe(this@MainActivity) { list ->
                    tvCount.text = "Tổng giao dịch: ${list.size}"
                }
        }
    }

    private fun checkPermissions() {
        val perms = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (flat?.contains(packageName) != true) {
            Toast.makeText(this, "Vui lòng bật Notification Access cho BankTracker", Toast.LENGTH_LONG).show()
            try { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
            catch (e: Exception) { }
        }
    }
}
