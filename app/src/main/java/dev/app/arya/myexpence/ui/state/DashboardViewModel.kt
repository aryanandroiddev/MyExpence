package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.app.arya.myexpence.data.repo.AnalyticsRepository
import dev.app.arya.myexpence.data.repo.SettingsRepository
import dev.app.arya.myexpence.domain.DashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class DashboardViewModel(
    private val analytics: AnalyticsRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _summary = MutableStateFlow<DashboardSummary?>(null)
    val summary: StateFlow<DashboardSummary?> = _summary.asStateFlow()

    fun loadForCurrentMonth() {
        val (start, end) = monthBoundsUtc(System.currentTimeMillis())
        viewModelScope.launch {
            val currency = settings.defaultCurrency.first()
            _summary.value = analytics.dashboardForMonth(start, end, currency)
        }
    }
}

private fun monthBoundsUtc(nowEpochMs: Long): Pair<Long, Long> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = nowEpochMs
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}

