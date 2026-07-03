package com.appcasa.features.documents.domain.usecase

import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDocumentsUseCase @Inject constructor(
  private val repository: DocumentRepository
) {
  operator fun invoke(hogarId: String): Flow<List<Document>> {
    return repository.getDocumentosByHogar(hogarId)
  }
}

class AddDocumentUseCase @Inject constructor(
  private val repository: DocumentRepository,
  private val reminderScheduler: ReminderScheduler
) {
  suspend operator fun invoke(
    hogarId: String,
    nombre: String,
    categoria: String,
    uriPdf: String,
    vencimiento: Long? = null
  ) {
    val id = java.util.UUID.randomUUID().toString()
    val doc = Document(
      id = id,
      hogarId = hogarId,
      nombre = nombre,
      categoria = categoria,
      uriPdf = uriPdf,
      fechaVencimiento = vencimiento
    )
    repository.upsertDocumento(doc)
        
    vencimiento?.let { date ->
      val alertDate = date - (30L * 24 * 60 * 60 * 1000)
      if (alertDate > System.currentTimeMillis()) {
        reminderScheduler.scheduleReminder(
          id = (id + 30000).hashCode(),
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
    reminderScheduler.cancelReminder((documento.id + 30000).hashCode())
  }
}

class UpdateDocumentUseCase @Inject constructor(
  private val repository: DocumentRepository,
  private val reminderScheduler: ReminderScheduler
) {
  suspend operator fun invoke(documento: Document) {
    repository.upsertDocumento(documento)
        
    reminderScheduler.cancelReminder((documento.id + 30000).hashCode())
    documento.fechaVencimiento?.let { date ->
      val alertDate = date - (30L * 24 * 60 * 60 * 1000)
      if (alertDate > System.currentTimeMillis()) {
        reminderScheduler.scheduleReminder(
          id = (documento.id + 30000).hashCode(),
          title = "Vencimiento Próximo: ${documento.nombre}",
          message = "El documento de la categoría ${documento.categoria} caduca en 30 días.",
          timeInMillis = alertDate
        )
      }
    }
  }
}
