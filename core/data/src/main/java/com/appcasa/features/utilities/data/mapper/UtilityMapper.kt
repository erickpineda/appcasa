package com.appcasa.features.utilities.data.mapper

import com.appcasa.core.domain.model.Utility
import com.appcasa.features.utilities.data.local.UtilidadEntity

fun UtilidadEntity.toDomain(): Utility {
    return Utility(
        id = id,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        icono = icono,
        activa = activa,
        orden = orden,
        categoria = categoria
    )
}

fun Utility.toEntity(): UtilidadEntity {
    return UtilidadEntity(
        id = id,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        icono = icono,
        activa = activa,
        orden = orden,
        categoria = categoria
    )
}
