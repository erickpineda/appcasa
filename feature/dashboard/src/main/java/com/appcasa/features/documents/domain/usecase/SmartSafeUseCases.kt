package com.appcasa.features.documents.domain.usecase

import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<Document>> {
        return repository.getDocumentosByHogar(hogarId)
    }
}

class AddDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        hogarId: Long,
        nombre: String,
        categoria: String,
        uriPdf: String,
        vencimiento: Long? = null
    ) {
        val doc = Document(
            hogarId = hogarId,
            nombre = nombre,
            categoria = categoria,
            uriPdf = uriPdf,
            fechaVencimiento = vencimiento
        )
        val id = repository.insertDocumento(doc)
        
        vencimiento?.let { date ->
            val alertDate = date - (30L * 24 * 60 * 60 * 1000)
            if (alertDate > System.currentTimeMillis()) {
              reminderScheduler.scheduleReminder(
                id = (id + 30000).toInt(),
                title = "Vencimiento Próximo: $nombre",
                message = "El documento de la categoría $categoria caduca en 30 días.",
                timeInMillis = alertDate
              )
            }
        }
    }
}

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(documento: Document) {
        repository.deleteDocumento(documento)
        reminderScheduler.cancelReminder((documento.id + 30000).toInt())
    }
}

class UpdateDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(documento: Document) {
        repository.insertDocumento(documento)
        
        reminderScheduler.cancelReminder((documento.id + 30000).toInt())
        documento.fechaVencimiento?.let { date ->
            val alertDate = date - (30L * 24 * 60 * 60 * 1000)
            if (alertDate > System.currentTimeMillis()) {
              reminderScheduler.scheduleReminder(
                id = (documento.id + 30000).toInt(),
                title = "Vencimiento Próximo: ${documento.nombre}",
                message = "El documento de la categoría ${documento.categoria} caduca en 30 días.",
                timeInMillis = alertDate
              )
            }
        }
    }
}
