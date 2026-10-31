package com.minnolter.habitrack.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.minnolter.habitrack.data.auth.FirebaseAuthManager
import com.minnolter.habitrack.domain.repository.HabitractRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirestoreSyncManager(
    private val repository: HabitractRepository,
    private val authManager: FirebaseAuthManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val firestore: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }

    fun startSyncing() {
        scope.launch {
            authManager.authState.collectLatest { authState ->
                if (authState.isAuthenticated && authState.uid != null) {
                    syncLocalToCloud(authState.uid)
                }
            }
        }
    }

    private fun syncLocalToCloud(userId: String) {
        val db = firestore ?: return
        scope.launch {
            repository.observeHabits().collectLatest { habits ->
                habits.forEach { habit ->
                    val habitMap = mapOf(
                        "id" to habit.id,
                        "name" to habit.name,
                        "description" to habit.description,
                        "colorHex" to habit.colorHex,
                        "imageUri" to habit.imageUri,
                        "sortOrder" to habit.sortOrder,
                        "createdAt" to habit.createdAt.toEpochMilli()
                    )
                    runCatching {
                        db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habit.id.toString())
                            .set(habitMap, SetOptions.merge())
                    }
                }
            }
        }
    }
}
