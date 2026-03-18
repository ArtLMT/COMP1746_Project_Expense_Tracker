package com.lmt.expensetracker.data.remote

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseService {
    // Trỏ đến nút gốc "sync_data" trên Firebase
    private val database = FirebaseDatabase.getInstance().getReference("sync_data")

    // Suspend để kh bị treo ui
    suspend fun uploadEverything(allData: Map<String, ProjectSyncModel>) {
        // setValue(allData) thực hiện yêu cầu "upload all at once" [cite: 95]
        database.setValue(allData).await()
    }
}