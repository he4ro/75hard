package com.challenge.hard75.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ────────────────────────────────────────────────────────────────

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "logs",
    indices = [Index(value = ["ruleId", "logicalDate"], unique = true)]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleId: Int,
    val logicalDate: String,   // "yyyy-MM-dd"
    val isCompleted: Boolean
)

@Entity(tableName = "challenge_state")
data class ChallengeState(
    @PrimaryKey val id: Int = 1,
    val startDateMillis: Long,
    val endOfDayHour: Int = 2  // Tasks reset at 2 AM
)

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface ChallengeDao {

    // Rules
    @Query("SELECT * FROM rules ORDER BY orderIndex ASC")
    fun getAllRules(): Flow<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)

    @Query("SELECT COUNT(*) FROM rules")
    suspend fun getRuleCount(): Int

    // Logs
    @Query("SELECT * FROM logs WHERE logicalDate = :date")
    fun getLogsForDate(date: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM logs WHERE logicalDate = :date")
    suspend fun getLogsForDateOnce(date: String): List<DailyLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog)

    @Query("DELETE FROM logs WHERE logicalDate = :date")
    suspend fun deleteLogsForDate(date: String)

    // All completed dates (for grid)
    @Query("SELECT DISTINCT logicalDate FROM logs WHERE isCompleted = 1 GROUP BY logicalDate HAVING COUNT(*) >= (SELECT COUNT(*) FROM rules)")
    fun getFullyCompletedDates(): Flow<List<String>>

    // Challenge state
    @Query("SELECT * FROM challenge_state WHERE id = 1")
    fun getChallengeState(): Flow<ChallengeState?>

    @Query("SELECT * FROM challenge_state WHERE id = 1")
    suspend fun getChallengeStateOnce(): ChallengeState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setChallengeState(state: ChallengeState)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [Rule::class, DailyLog::class, ChallengeState::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "challenge_database"
                ).build().also { INSTANCE = it }
            }
    }
}
