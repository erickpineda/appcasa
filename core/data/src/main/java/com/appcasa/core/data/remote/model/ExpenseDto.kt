package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Expense

data class ExpenseDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val concepto: String = "",
    val importe: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val categoria: String = "",
    val fotoUri: String? = null,
    val archived: Boolean = false,
    val syncId: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val createdById: Long? = null
) {
    fun toDomain(): Expense = Expense(
        id = id,
        hogarId = hogarId,
        concepto = concepto,
        importe = importe,
        fecha = fecha,
        categoria = categoria,
        fotoUri = fotoUri,
        archived = archived,
        syncId = syncId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdById = createdById
    )

    companion object {
        fun fromDomain(expense: Expense): ExpenseDto = ExpenseDto(
            id = expense.id,
            hogarId = expense.hogarId,
            concepto = expense.concepto,
            importe = expense.importe,
            fecha = expense.fecha,
            categoria = expense.categoria,
            fotoUri = expense.fotoUri,
            archived = expense.archived,
            syncId = expense.syncId,
            createdAt = expense.createdAt,
            updatedAt = expense.updatedAt,
            createdById = expense.createdById
        )
    }
}
