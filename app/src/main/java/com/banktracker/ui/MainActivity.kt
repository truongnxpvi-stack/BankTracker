package com.banktracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ẩn toàn bộ nội dung trước khi xác thực xong
        findViewById<View>(R.id.mainContent).visibility = View.INVISIBLE

        // Xác thực vân tay ngay khi mở app
        authenticateUser()
    }

    private fun authenticateUser() {
        if (!BiometricHelper.canAuthenticate(this)) {
            // Thiết bị không hỗ trợ sinh trắc → mở thẳng
            onAuthSuccess()
            return
        }

        BiometricHelper.showPrompt(
            activity = this,
            onSuccess = {
                onAuthSuccess()
            },
            onFailed = {
                // Vân tay sai → thử lại tự động (BiometricPrompt tự xử lý)
            },
            onError = { msg ->
                Toast.makeText(this, "Lỗi xác thực: $msg", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun onAuthSuccess() {
        // Hiện nội dung sau khi xác thực thành công
        findViewById<View>(R.id.mainContent).visibility = View.VISIBLE
        checkPermissions()
        loadData()

        findViewById<android.widget.Button>(R.id.btnHistory).setOnClickListener {
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
                    runOnUiThread {
                        try {
                            findViewById<TextView>(R.id.tvExpense).text =
                                "Chi tháng này: ${fmt.format(total ?: 0)} đ"
                        } catch (e: Exception) { }
                    }
                }

            db.transactionDao().getTotalIncomeByMonth(thisMonth)
                .observe(this@MainActivity) { total ->
                    runOnUiThread {
                        try {
                            findViewById<TextView>(R.id.tvIncome).text =
                                "Thu tháng này: ${fmt.format(total ?: 0)} đ"
                        } catch (e: Exception) { }
                    }
                }

            db.transactionDao().getAll().observe(this@MainActivity) { list ->
                runOnUiThread {
                    try {
                        findViewById<TextView>(R.id.tvCount).text =
                            "Tổng giao dịch: ${list.size}"
                    } catch (e: Exception) { }
                }
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

        val flat = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        )
        if (flat?.contains(packageName) != true) {
            Toast.makeText(
                this,
                "Vui lòng bật Notification Access cho BankTracker",
                Toast.LENGTH_LONG
            ).show()
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) { }
        }
    }
}
