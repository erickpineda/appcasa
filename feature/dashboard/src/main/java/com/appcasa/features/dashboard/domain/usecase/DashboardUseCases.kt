package com.appcasa.features.dashboard.domain.usecase

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Task
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.presentation.model.SearchType
import com.appcasa.navigation.Screen
import kotlinx.coroutines.flow.*
import com.appcasa.core.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetPostItsUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  operator fun invoke(hogarId: Long): Flow<List<PostIt>> {
    return repository.getPostIts(hogarId)
  }
}

class AddPostItUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  suspend operator fun invoke(hogarId: Long, contenido: String, color: String = "#FFF9C4") {
    repository.insertPostIt(PostIt(hogarId = hogarId, contenido = contenido, colorHex = color))
  }
}

class UpdatePostItUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  suspend operator fun invoke(postIt: PostIt) {
    repository.insertPostIt(postIt)
  }
}

class DeletePostItUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  suspend operator fun invoke(postIt: PostIt) {
    repository.deletePostIt(postIt)
  }
}

class GetDashboardConfigUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  operator fun invoke(hogarId: Long): Flow<DashboardConfig?> {
    return repository.getDashboardConfig(hogarId)
  }
}

class UpdateDashboardOrderUseCase @Inject constructor(
  private val repository: DashboardRepository
) {
  suspend operator fun invoke(hogarId: Long, newOrder: List<String>) {
    repository.saveDashboardConfig(
      DashboardConfig(
        hogarId = hogarId,
        ordenModulos = newOrder.joinToString(",")
      )
    )
  }
}

class SearchUseCase @Inject constructor(
  private val tasksRepository: TasksRepository,
  private val listsRepository: ListsRepository,
  private val familyRepository: FamilyRepository,
  private val inventoryRepository: InventoryRepository,
  private val financeRepository: FinanceRepository,
  private val maintenanceRepository: MaintenanceRepository
) {
  suspend operator fun invoke(hogarId: Long, query: String): List<SearchItem> {
    val tasks = tasksRepository.getTasksByHogar(hogarId).first()
    val lists = listsRepository.getListasPaged(hogarId, 50, 0).first()
    val members = familyRepository.getMembersByHogar(hogarId).first()
    val stock = inventoryRepository.getStockByHogar(hogarId).first()
    val expenses = financeRepository.getExpensesByHogar(hogarId).first()
    val maintenance = maintenanceRepository.getEventsPaged(hogarId, 50, 0).first()

    val results = mutableListOf<SearchItem>()

    results.addAll(
      tasks.filter { it.titulo.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.titulo, SearchType.TASK, Icons.Default.Task, Screen.TaskDetail(it.id)) }
    )
    results.addAll(
      lists.filter { it.nombre.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.nombre, SearchType.LIST, Icons.AutoMirrored.Filled.List, Screen.ListDetail(it.id)) }
    )
    results.addAll(
      members.filter { it.nombre.contains(query, ignoreCase = true) }
        .map {
          val route = if (it.tipo == TipoMiembro.PERSONA) Screen.MemberDetail(it.id) else Screen.PetDetail(it.id)
          SearchItem(it.id, it.nombre, SearchType.MEMBER, if (it.tipo == TipoMiembro.PERSONA) Icons.Default.Person else Icons.Default.Pets, route)
        }
    )
    results.addAll(
      stock.filter { it.nombre.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.nombre, SearchType.STOCK, Icons.Default.Inventory, Screen.Inventory) }
    )
    results.addAll(
      expenses.filter { it.concepto.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.concepto, SearchType.EXPENSE, Icons.Default.Payments, Screen.Expenses) }
    )
    results.addAll(
      maintenance.filter { it.titulo.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.titulo, SearchType.MAINTENANCE, Icons.Default.Build, Screen.MaintenanceDetail(it.id)) }
    )

    return results
  }
}

class GetNextEventUseCase @Inject constructor(
  private val calendarRepository: CalendarRepository,
  private val reminderRepository: ReminderRepository,
  private val tasksRepository: TasksRepository,
  private val familyRepository: FamilyRepository
) {
  operator fun invoke(hogarId: Long): Flow<NextEventSummary?> {
    return combine(
      calendarRepository.getEventsByHogar(hogarId),
      reminderRepository.getRemindersByHogar(hogarId),
      tasksRepository.getTasksByHogar(hogarId),
      familyRepository.getMembersByHogar(hogarId)
    ) { eventos, recordatorios, tareas, miembros ->
      val birthdays = miembros.filter { it.fechaNacimiento != null }.map {
        NextEventSummary(it.nombre, calculateBirthdayOccurrence(it.fechaNacimiento!!), true)
      }

      val proximo = (eventos.map { NextEventSummary(it.titulo, it.fecha) } +
        recordatorios.map { NextEventSummary(it.titulo, it.fechaHora) } +
        tareas.filter { it.fechaLimite != null && it.estado != EstadoTarea.COMPLETADA }.map { NextEventSummary(it.titulo, it.fechaLimite!!) } +
        birthdays)
        .filter { it.timestamp >= System.currentTimeMillis() }
        .sortedBy { it.timestamp }
        .firstOrNull()

      proximo
    }
  }

  private fun calculateBirthdayOccurrence(birthDateMillis: Long): Long {
    val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
    val today = Calendar.getInstance()

    val occurrence = Calendar.getInstance().apply {
      set(Calendar.YEAR, today.get(Calendar.YEAR))
      set(Calendar.MONTH, birthDate.get(Calendar.MONTH))
      set(Calendar.DAY_OF_MONTH, birthDate.get(Calendar.DAY_OF_MONTH))
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return occurrence.timeInMillis
  }

  private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
      Constants.Formatting.DAY_MONTH_ALL_DAY_ES
    } else {
      Constants.Formatting.DAY_MONTH_TIME_ES
    }
    val sdf = SimpleDateFormat(format, Constants.Locales.SPAIN)
    return sdf.format(date)
  }
}
