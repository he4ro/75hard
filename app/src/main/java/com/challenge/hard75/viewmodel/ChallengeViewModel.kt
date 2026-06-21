package com.challenge.hard75.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.challenge.hard75.data.AppDatabase
import com.challenge.hard75.data.ChallengeState
import com.challenge.hard75.data.DailyLog
import com.challenge.hard75.data.Rule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class UiState(
    val rules: List<Rule> = emptyList(),
    val completedRuleIds: Set<Int> = emptySet(),
    val currentDay: Int = 1,
    val challengeStarted: Boolean = false,
    val fullyCompletedDates: List<String> = emptyList(),
    val logicalDate: String = "",
    val endOfDayHour: Int = 2,
    val allDoneToday: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).challengeDao()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getChallengeState(),
                dao.getAllRules(),
                dao.getFullyCompletedDates()
            ) { state, rules, completedDates ->
                Triple(state, rules, completedDates)
            }.flatMapLatest { (state, rules, completedDates) ->
                val endHour = state?.endOfDayHour ?: 2
                val logicalDate = getLogicalDate(endHour)
                dao.getLogsForDate(logicalDate).map { logs ->
                    val completedIds = logs.filter { it.isCompleted }.map { it.ruleId }.toSet()
                    UiState(
                        rules = rules,
                        completedRuleIds = completedIds,
                        currentDay = calculateDay(state),
                        challengeStarted = state != null,
                        fullyCompletedDates = completedDates,
                        logicalDate = logicalDate,
                        endOfDayHour = endHour,
                        allDoneToday = rules.isNotEmpty() && completedIds.size == rules.size
                    )
                }
            }.collect { _uiState.value = it }
        }
    }

    fun getLogicalDate(endOfDayHour: Int = _uiState.value.endOfDayHour): String {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) < endOfDayHour) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun calculateDay(state: ChallengeState?): Int {
        state ?: return 1
        val now = Calendar.getInstance()
        val start = Calendar.getInstance().apply { timeInMillis = state.startDateMillis }
        // Normalize both to midnight
        now.set(Calendar.HOUR_OF_DAY, 0); now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0); now.set(Calendar.MILLISECOND, 0)
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0)
        val diff = ((now.timeInMillis - start.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        return diff.coerceIn(1, 75)
    }

    fun startChallenge() {
        viewModelScope.launch {
            dao.setChallengeState(ChallengeState(startDateMillis = System.currentTimeMillis()))
        }
    }

    fun resetChallenge() {
        viewModelScope.launch {
            dao.setChallengeState(ChallengeState(startDateMillis = System.currentTimeMillis()))
        }
    }

    fun toggleRule(ruleId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val date = _uiState.value.logicalDate
            dao.insertLog(DailyLog(ruleId = ruleId, logicalDate = date, isCompleted = isCompleted))
        }
    }

    fun addRule(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val count = dao.getRuleCount()
            dao.insertRule(Rule(text = text.trim(), orderIndex = count))
        }
    }

    fun deleteRule(rule: Rule) {
        viewModelScope.launch { dao.deleteRule(rule) }
    }

    fun setEndOfDayHour(hour: Int) {
        viewModelScope.launch {
            val current = dao.getChallengeStateOnce() ?: return@launch
            dao.setChallengeState(current.copy(endOfDayHour = hour))
        }
    }

    // Expose for worker
    suspend fun getChallengeStateOnce() = dao.getChallengeStateOnce()
}
