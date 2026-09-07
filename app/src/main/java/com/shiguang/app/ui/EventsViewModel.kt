package com.shiguang.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiguang.app.data.AppDatabase
import com.shiguang.app.data.EventEntity
import com.shiguang.app.logic.EventUi
import com.shiguang.app.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).eventDao()

    val events: StateFlow<List<EventUi>> = dao.observeAll()
        .map { list -> list.map { EventUi.from(it, LocalDate.now()) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(
        id: Long?,
        title: String,
        targetEpochDay: Long,
        startEpochDay: Long?,
        repeatYearly: Boolean,
        colorIndex: Int,
        note: String,
    ) {
        viewModelScope.launch {
            if (id == null) {
                dao.insert(
                    EventEntity(
                        title = title,
                        targetEpochDay = targetEpochDay,
                        startEpochDay = startEpochDay,
                        repeatYearly = repeatYearly,
                        colorIndex = colorIndex,
                        note = note,
                    )
                )
            } else {
                val old = dao.byId(id) ?: return@launch
                dao.update(
                    old.copy(
                        title = title,
                        targetEpochDay = targetEpochDay,
                        startEpochDay = startEpochDay,
                        repeatYearly = repeatYearly,
                        colorIndex = colorIndex,
                        note = note,
                    )
                )
            }
            WidgetUpdater.updateAll(getApplication())
        }
    }

    fun setStyle(id: Long, styleIndex: Int) {
        viewModelScope.launch {
            dao.byId(id)?.let { dao.update(it.copy(styleIndex = styleIndex)) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.byId(id)?.let { dao.delete(it) }
            WidgetUpdater.updateAll(getApplication())
        }
    }
}
