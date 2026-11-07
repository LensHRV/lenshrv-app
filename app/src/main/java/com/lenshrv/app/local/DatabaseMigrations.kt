package com.lenshrv.app.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hrv_metrics ADD COLUMN tag TEXT NOT NULL DEFAULT ''")
    }
}
