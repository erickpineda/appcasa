package com.appcasa.core.data.remote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.domain.repository.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hogarId = inputData.getLong("hogarId", -1L)
        if (hogarId == -1L) return Result.failure()

        return try {
            // Fetch all tasks for this household from local DB
            val localTasks = tasksRepository.getTasksByHogar(hogarId).first()
            
            // Upload each to Firestore
            localTasks.forEach { task ->
                firestoreDataSource.syncTask(task)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
