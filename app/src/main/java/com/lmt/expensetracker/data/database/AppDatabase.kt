package com.lmt.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.entities.ProjectEntity

@Database(
    entities = [ProjectEntity::class, ExpenseEntity::class],
    version = 2,          // ─ bumped from 1: added updatedAt + isDeleted to both tables
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from v1 → v2.
         * Adds `updatedAt` (Long, epoch ms) and `isDeleted` (Boolean, stored as Int)
         * to both the `projects` and `expenses` tables.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE projects ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE projects ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE expenses ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE expenses ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2)      // run migration first
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
