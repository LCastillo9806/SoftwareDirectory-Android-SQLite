
package co.edu.unal.softwaredirectory

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "software_directory.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_COMPANIES = "companies"

        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_WEBSITE = "website"
        const val COL_PHONE = "phone"
        const val COL_EMAIL = "email"
        const val COL_SERVICES = "services"
        const val COL_CLASSIFICATION = "classification"
    }

    // Crear tabla SQLite
    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE $TABLE_COMPANIES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_WEBSITE TEXT,
                $COL_PHONE TEXT,
                $COL_EMAIL TEXT,
                $COL_SERVICES TEXT,
                $COL_CLASSIFICATION TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    // No se requiere migración porque mantenemos
    // exactamente la misma estructura de la tabla
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Reservado para futuras migraciones
    }

    // Convertir un objeto Company en ContentValues
    private fun companyValues(company: Company): ContentValues {

        return ContentValues().apply {
            put(COL_NAME, company.name)
            put(COL_WEBSITE, company.website)
            put(COL_PHONE, company.phone)
            put(COL_EMAIL, company.email)
            put(COL_SERVICES, company.services)
            put(COL_CLASSIFICATION, company.classification)
        }
    }

    // Convertir una fila SQLite en objeto Company
    private fun cursorToCompany(cursor: Cursor): Company {

        return Company(
            id = cursor.getInt(
                cursor.getColumnIndexOrThrow(COL_ID)
            ),
            name = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_NAME)
            ) ?: "",
            website = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_WEBSITE)
            ) ?: "",
            phone = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_PHONE)
            ) ?: "",
            email = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_EMAIL)
            ) ?: "",
            services = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_SERVICES)
            ) ?: "",
            classification = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_CLASSIFICATION)
            ) ?: ""
        )
    }

    // CREATE: registrar empresa
    fun insertCompany(company: Company): Long {

        return writableDatabase.insert(
            TABLE_COMPANIES,
            null,
            companyValues(company)
        )
    }

    // READ: consultar todas las empresas
    fun getAllCompanies(): List<Company> {

        return searchCompanies()
    }

    // READ: buscar por nombre y/o clasificación
    fun searchCompanies(
        name: String = "",
        classification: String = ""
    ): List<Company> {

        val companies = mutableListOf<Company>()
        val conditions = mutableListOf<String>()
        val arguments = mutableListOf<String>()

        // Filtro por nombre
        if (name.isNotBlank()) {

            conditions.add("$COL_NAME LIKE ? ESCAPE '\\'")

            val safeName = name.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")

            arguments.add("%$safeName%")
        }

        // Filtro por clasificación múltiple
        if (classification.isNotBlank()) {

            // Las clasificaciones se guardan separadas
            // por comas, por ejemplo:
            // "Consultoría, Desarrollo a la medida"

            // Se buscan categorías completas, no fragmentos
            // de otras palabras.
            conditions.add(
                "(',' || REPLACE($COL_CLASSIFICATION, ', ', ',') || ',') " +
                        "LIKE ? ESCAPE '\\'"
            )

            val safeClassification = classification.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")

            arguments.add("%,$safeClassification,%")
        }

        val selection = if (conditions.isEmpty()) {
            null
        } else {
            conditions.joinToString(" AND ")
        }

        val selectionArgs = if (arguments.isEmpty()) {
            null
        } else {
            arguments.toTypedArray()
        }

        val cursor = readableDatabase.query(
            TABLE_COMPANIES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_NAME COLLATE NOCASE ASC"
        )

        cursor.use {

            while (it.moveToNext()) {
                companies.add(cursorToCompany(it))
            }
        }

        return companies
    }

    // READ: obtener empresa por ID
    fun getCompanyById(id: Int): Company? {

        val cursor = readableDatabase.query(
            TABLE_COMPANIES,
            null,
            "$COL_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {

            if (it.moveToFirst()) {
                return cursorToCompany(it)
            }
        }

        return null
    }

    // UPDATE: actualizar empresa
    fun updateCompany(company: Company): Int {

        return writableDatabase.update(
            TABLE_COMPANIES,
            companyValues(company),
            "$COL_ID = ?",
            arrayOf(company.id.toString())
        )
    }

    // DELETE: eliminar empresa
    fun deleteCompany(id: Int): Int {

        return writableDatabase.delete(
            TABLE_COMPANIES,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
    }
}

