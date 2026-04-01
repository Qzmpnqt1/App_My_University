package com.example.app_my_university.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.app_my_university.data.repository.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val chatRepository: ChatRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            chatRepository.getConversations()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "moi_vuz_background_sync"
    }
}
