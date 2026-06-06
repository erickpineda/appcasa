package com.appcasa.features.finance.domain.usecase

import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.UserRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import javax.inject.Inject
import kotlin.coroutines.resume

data class OcrResult(val total: Double?, val store: String?)

class ProcessTicketUseCase @Inject constructor() {
    suspend operator fun invoke(bitmap: android.graphics.Bitmap): OcrResult = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(interpretText(visionText.text))
            }
            .addOnFailureListener {
                continuation.resume(OcrResult(null, null))
            }
    }

    fun interpretText(text: String): OcrResult {
        val prices = Regex("""\d+[.,]\d{2}""").findAll(text)
            .map { it.value.replace(",", ".").toDouble() }
            .toList()
        val total = prices.maxOrNull()

        val lines = text.lines().filter { it.isNotBlank() }
        val store = lines.firstOrNull { !it.contains(Regex("""\d{2}/\d{2}""")) }

        return OcrResult(total, store)
    }
}

class GetExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Expense>> {
        return repository.getExpensesPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class GetArchivedExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Expense>> {
        return repository.getArchivedExpensesPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class AddExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(hogarId: Long, concepto: String, importe: Double, categoria: String, fotoUri: String? = null) {
        val currentUser = userRepository.getCurrentUser().first()
        repository.insertExpense(
            Expense(
                hogarId = hogarId,
                concepto = concepto,
                importe = importe,
                categoria = categoria,
                fotoUri = fotoUri,
                createdById = currentUser?.id
            )
        )
    }
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.deleteExpense(expense)
    }
}

class UnarchiveExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expenseId: Long) {
        repository.unarchiveExpense(expenseId)
    }
}

class ClearAllArchivedExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        repository.deleteAllExpenses(hogarId)
    }
}

class ArchiveOldExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        repository.archiveOldExpenses(hogarId, threshold)
    }
}

class PurgeOldPhotosUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        repository.purgeOldExpensePhotos(hogarId, threshold)
    }
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.insertExpense(expense)
    }
}

class GetExpensesByCategoryUseCase @Inject constructor() {
    operator fun invoke(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.importe } }
    }
}

class GetMonthlyEvolutionUseCase @Inject constructor() {
    operator fun invoke(expenses: List<Expense>): Map<String, Double> {
        val sdf = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
        return expenses.groupBy { sdf.format(java.util.Date(it.fecha)) }
            .mapValues { entry -> entry.value.sumOf { it.importe } }
    }
}
