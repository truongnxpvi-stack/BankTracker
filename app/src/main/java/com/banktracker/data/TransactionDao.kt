package com.banktracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

data class MonthlyTotal(val date: String, val total: Long)
data class CategoryTotal(val category: String, val total: Long)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT date, SUM(amount) as total FROM transactions WHERE type = 'DEBIT' GROUP BY date ORDER BY date DESC")
    fun getMonthlyExpense(): LiveData<List<MonthlyTotal>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'DEBIT' GROUP BY category ORDER BY total DESC")
    fun getByCategory(): LiveData<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND date = :month")
    fun getTotalExpenseByMonth(month: String): LiveData<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT' AND date = :month")
    fun getTotalIncomeByMonth(month: String): LiveData<Long?>
}
