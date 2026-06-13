package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Expense

data class ExpenseDto(
    val syncId: String? = null,
    val hogarSyncId: String? = null,
    val concepto: String = "",
    val importe: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val categoria: String = "",
    val fotoUri: String? = null,
    val archived: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val createdBySyncId: String? = null
) {
    fun toDomain(): Expense = Expense(
        id = 0,
        syncId = syncId,
        hogarId = 0,
        hogarSyncId = hogarSyncId,
        concepto = concepto,
        importe = importe,
        fecha = fecha,
        categoria = categoria,
        fotoUri = fotoUri,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBySyncId = createdBySyncId
    )

    companion object {
        fun fromDomain(expense: Expense): ExpenseDto = ExpenseDto(
            syncId = expense.syncId,
            hogarSyncId = expense.hogarSyncId,
            concepto = expense.concepto,
            importe = expense.importe,
            fecha = expense.fecha,
            categoria = expense.categoria,
            fotoUri = expense.fotoUri,
            archived = expense.archived,
            createdAt = expense.createdAt,
            updatedAt = expense.updatedAt,
            createdBySyncId = expense.createdBySyncId
        )
    }
}
