package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.PostIt

data class PostItDto(
    val syncId: String? = null,
    val hogarSyncId: String? = null,
    val contenido: String = "",
    val colorHex: String = "#FFF9C4",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): PostIt = PostIt(
        id = 0,
        syncId = syncId,
        hogarId = 0,
        hogarSyncId = hogarSyncId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(postIt: PostIt): PostItDto = PostItDto(
            syncId = postIt.syncId,
            hogarSyncId = postIt.hogarSyncId,
            contenido = postIt.contenido,
            colorHex = postIt.colorHex,
            createdAt = postIt.createdAt,
            updatedAt = postIt.updatedAt
        )
    }
}
