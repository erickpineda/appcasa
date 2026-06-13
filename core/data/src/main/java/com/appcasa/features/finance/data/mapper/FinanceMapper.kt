package com.appcasa.features.finance.data.mapper

import com.appcasa.core.domain.model.Expense
import com.appcasa.features.finance.data.local.ExpenseEntity

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        concepto = concepto,
        importe = importe,
        categoria = categoria,
        fecha = fecha,
        fotoUri = fotoUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdById = createdById,
        createdBySyncId = createdBySyncId,
        archived = archived,
        lastSyncedAt = lastSyncedAt
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        concepto = concepto,
        importe = importe,
        categoria = categoria,
        fecha = fecha,
        fotoUri = fotoUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdById = createdById,
        createdBySyncId = createdBySyncId,
        archived = archived,
        lastSyncedAt = lastSyncedAt
    )
}
