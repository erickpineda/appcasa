package com.appcasa.features.finance.data.mapper

import com.appcasa.core.domain.model.Expense
import com.appcasa.features.finance.data.local.ExpenseEntity

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        hogarId = hogarId,
        concepto = concepto,
        importe = importe,
        categoria = categoria,
        fecha = fecha,
        fotoUri = fotoUri,
        createdAt = createdAt,
        createdById = createdById,
        archived = archived
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        hogarId = hogarId,
        concepto = concepto,
        importe = importe,
        categoria = categoria,
        fecha = fecha,
        fotoUri = fotoUri,
        createdAt = createdAt,
        createdById = createdById,
        archived = archived
    )
}
