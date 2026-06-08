package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.PostIt

data class PostItDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val contenido: String = "",
    val colorHex: String = "#FFF9C4",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val syncId: String? = null
) {
    fun toDomain(): PostIt = PostIt(
        id = id,
        hogarId = hogarId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )

    companion object {
        fun fromDomain(postIt: PostIt): PostItDto = PostItDto(
            id = postIt.id,
            hogarId = postIt.hogarId,
            contenido = postIt.contenido,
            colorHex = postIt.colorHex,
            createdAt = postIt.createdAt,
            updatedAt = postIt.updatedAt,
            syncId = postIt.syncId
        )
    }
}
