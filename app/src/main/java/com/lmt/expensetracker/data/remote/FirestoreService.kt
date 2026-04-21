package com.lmt.expensetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.entities.ProjectEntity
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore data-source layer.
 *
 * Provides suspend CRUD operations against two top-level collections:
 *  - **"projects"**  – keyed by [ProjectEntity.projectId]
 *  - **"expenses"**  – keyed by [ExpenseEntity.expenseId], each carrying a
 *                       `projectId` field for querying.
 *
 * All functions use `kotlinx-coroutines-play-services` `.await()` to bridge
 * Firebase Tasks → coroutines.
 *
 * @property db The [FirebaseFirestore] instance (defaults to the singleton).
 */
class FirestoreService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ──────────────────────── Collection References ────────────────────────

    private companion object {
        /** Top-level Firestore collection for projects. */
        const val PROJECTS_COLLECTION = "projects"

        /** Top-level Firestore collection for expenses. */
        const val EXPENSES_COLLECTION = "expenses"
    }

    // ──────────────────────── PROJECT OPERATIONS ────────────────────────

    /**
     * Creates **or fully replaces** a project document in Firestore.
     *
     * The document ID is [ProjectEntity.projectId]; if the document already
     * exists it will be overwritten.
     *
     * @param project The project to persist.
     * @throws Exception if the Firestore write fails.
     */
    suspend fun createProject(project: ProjectEntity) {
        db.collection(PROJECTS_COLLECTION)
            .document(project.projectId)
            .set(project.toFirestoreMap())
            .await()
    }

    /**
     * Fetches **all** documents from the "projects" collection and maps them
     * back to [ProjectEntity] instances.
     *
     * @return A (potentially empty) list of projects.
     * @throws Exception if the Firestore read fails.
     */
    suspend fun getProjects(): List<ProjectEntity> {
        val snapshot = db.collection(PROJECTS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toProjectEntity()
        }
    }

    /**
     * Deletes a single project document by its ID.
     *
     * @param projectId ID of the project to delete.
     * @throws Exception if the Firestore delete fails.
     */
    suspend fun deleteProject(projectId: String) {
        db.collection(PROJECTS_COLLECTION)
            .document(projectId)
            .delete()
            .await()
    }

    // ──────────────────────── EXPENSE OPERATIONS ────────────────────────

    /**
     * Creates **or fully replaces** an expense document in Firestore.
     *
     * The document ID is [ExpenseEntity.expenseId]; each document stores
     * a `projectId` field for querying expenses by project.
     *
     * @param expense The expense to persist.
     * @throws Exception if the Firestore write fails.
     */
    suspend fun addExpense(expense: ExpenseEntity) {
        db.collection(EXPENSES_COLLECTION)
            .document(expense.expenseId)
            .set(expense.toFirestoreMap())
            .await()
    }

    /**
     * Fetches all expenses whose `projectId` field equals [projectId].
     *
     * Uses a Firestore **`whereEqualTo`** query – which is indexed
     * automatically for single-field equality queries.
     *
     * @param projectId The parent project's ID.
     * @return A (potentially empty) list of expenses for that project.
     * @throws Exception if the Firestore read fails.
     */
    suspend fun getExpensesByProject(projectId: String): List<ExpenseEntity> {
        val snapshot = db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("projectId", projectId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toExpenseEntity()
        }
    }

    /**
     * Fetches **all** documents from the "expenses" collection and maps them
     * back to [ExpenseEntity] instances.
     *
     * @return A (potentially empty) list of all expenses.
     * @throws Exception if the Firestore read fails.
     */
    suspend fun getAllExpenses(): List<ExpenseEntity> {
        val snapshot = db.collection(EXPENSES_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toExpenseEntity()
        }
    }

    /**
     * Deletes a single expense document by its ID.
     *
     * @param expenseId ID of the expense to delete.
     * @throws Exception if the Firestore delete fails.
     */
    suspend fun deleteExpense(expenseId: String) {
        db.collection(EXPENSES_COLLECTION)
            .document(expenseId)
            .delete()
            .await()
    }

    // ──────────────────── BULK SYNC (migration helper) ─────────────────────

    /**
     * Uploads all local projects **and** expenses to Firestore in a single
     * batched write.
     *
     * Firestore batched writes are atomic: either every document is written
     * or none is. Batch limit is 500 operations; this method handles
     * arbitrarily large datasets by splitting into multiple batches.
     *
     * **Use case:** Full sync after local mutations (same pattern you had
     * with `FirebaseService.uploadEverything()`).
     *
     * @param projects All local projects.
     * @param expenses All local expenses.
     * @throws Exception if any batch commit fails.
     */
    suspend fun syncAll(
        projects: List<ProjectEntity>,
        expenses: List<ExpenseEntity>
    ) {
        // Firestore batch limit is 500 operations per batch.
        // We create document-set operations for every project + every expense.
        val allOperations = mutableListOf<Pair<String, Map<String, Any?>>>()

        // Prepare project operations
        for (project in projects) {
            allOperations.add(
                "$PROJECTS_COLLECTION/${project.projectId}" to project.toFirestoreMap()
            )
        }

        // Prepare expense operations
        for (expense in expenses) {
            allOperations.add(
                "$EXPENSES_COLLECTION/${expense.expenseId}" to expense.toFirestoreMap()
            )
        }

        // Split into chunks of 500 (Firestore batch limit)
        allOperations.chunked(500).forEach { chunk ->
            val batch = db.batch()
            for ((path, data) in chunk) {
                val docRef = db.document(path)
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    // ──────────────────── MAPPING HELPERS (private) ─────────────────────

    /**
     * Converts a [ProjectEntity] to a Firestore-friendly [Map].
     *
     * We use an explicit map rather than relying on Firestore's automatic
     * POJO serialization so we have full control over field names and
     * avoid accidental Room-annotation leakage.
     */
    private fun ProjectEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "projectId"            to projectId,
        "name"                 to name,
        "description"          to description,
        "startDate"            to startDate,
        "endDate"              to endDate,
        "manager"              to manager,
        "status"               to status,
        "budget"               to budget,
        "specialRequirements"  to specialRequirements,
        "clientDepartmentInfo" to clientDepartmentInfo,
        "updatedAt"            to updatedAt,   // epoch ms — Last-Write-Wins key
        "isDeleted"            to isDeleted    // tombstone flag
    )

    /**
     * Converts a [ExpenseEntity] to a Firestore-friendly [Map].
     */
    private fun ExpenseEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "expenseId"     to expenseId,
        "projectId"     to projectId,
        "date"          to date,
        "amount"        to amount,
        "currency"      to currency,
        "type"          to type,
        "paymentMethod" to paymentMethod,
        "claimant"      to claimant,
        "status"        to status,
        "description"   to description,
        "location"      to location,
        "updatedAt"     to updatedAt,  // epoch ms — Last-Write-Wins key
        "isDeleted"     to isDeleted   // tombstone flag
    )

    /**
     * Safely maps a Firestore document snapshot to a [ProjectEntity].
     * Returns `null` if any required field is missing.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toProjectEntity(): ProjectEntity? {
        return try {
            ProjectEntity(
                projectId            = getString("projectId") ?: id,
                name                 = getString("name") ?: return null,
                description          = getString("description") ?: "",
                startDate            = getString("startDate") ?: return null,
                endDate              = getString("endDate") ?: return null,
                manager              = getString("manager") ?: "",
                status               = getString("status") ?: "Active",
                budget               = getDouble("budget") ?: 0.0,
                specialRequirements  = getString("specialRequirements") ?: "",
                clientDepartmentInfo = getString("clientDepartmentInfo") ?: "",
                updatedAt            = getLong("updatedAt") ?: 0L,
                isDeleted            = getBoolean("isDeleted") ?: false
            )
        } catch (e: Exception) {
            null // Skip malformed documents gracefully
        }
    }

    /**
     * Safely maps a Firestore document snapshot to an [ExpenseEntity].
     * Returns `null` if any required field is missing.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toExpenseEntity(): ExpenseEntity? {
        return try {
            ExpenseEntity(
                expenseId     = getString("expenseId") ?: id,
                projectId     = getString("projectId") ?: return null,
                date          = getString("date") ?: return null,
                amount        = getDouble("amount") ?: return null,
                currency      = getString("currency") ?: "USD",
                type          = getString("type") ?: return null,
                paymentMethod = getString("paymentMethod") ?: return null,
                claimant      = getString("claimant") ?: return null,
                status        = getString("status") ?: "Pending",
                description   = getString("description") ?: "",
                location      = getString("location") ?: "",
                updatedAt     = getLong("updatedAt") ?: 0L,
                isDeleted     = getBoolean("isDeleted") ?: false
            )
        } catch (e: Exception) {
            null // Skip malformed documents gracefully
        }
    }
}
