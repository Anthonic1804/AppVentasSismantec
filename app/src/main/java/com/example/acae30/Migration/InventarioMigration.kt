package com.example.acae30.Migration

// Migrations.kt
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS inventario (
                id INTEGER PRIMARY KEY NOT NULL,
                bonificado REAL NOT NULL,
                codigo TEXT NOT NULL,
                costo REAL NOT NULL,
                costo_iva REAL NOT NULL,
                descripcion TEXT NOT NULL,
                existencia REAL NOT NULL,
                existencia_u REAL NOT NULL,
                fraccion REAL NOT NULL,
                precio REAL NOT NULL,
                precio2 REAL NOT NULL,
                precio2_iva REAL NOT NULL,
                precio_iva REAL NOT NULL,
                precio_u REAL NOT NULL,
                precio_u2 REAL NOT NULL,
                precio_u2_iva REAL NOT NULL,
                precio_u_iva REAL NOT NULL,
                precio_viñeta REAL NOT NULL,
                precio_viñeta_iva REAL NOT NULL
            )
        """.trimIndent())
    }
}
