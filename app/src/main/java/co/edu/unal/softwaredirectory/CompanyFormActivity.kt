
package co.edu.unal.softwaredirectory

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CompanyFormActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_form)

        // Inicializar base de datos SQLite
        databaseHelper = DatabaseHelper(this)

        // Conectar campos del formulario
        val tvFormTitle = findViewById<TextView>(R.id.tvFormTitle)
        val etName = findViewById<EditText>(R.id.etCompanyName)
        val etWebsite = findViewById<EditText>(R.id.etWebsite)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etServices = findViewById<EditText>(R.id.etServices)

        // Clasificaciones múltiples
        val cbConsulting = findViewById<CheckBox>(R.id.cbConsulting)
        val cbCustomDevelopment =
            findViewById<CheckBox>(R.id.cbCustomDevelopment)
        val cbSoftwareFactory =
            findViewById<CheckBox>(R.id.cbSoftwareFactory)

        val btnSave = findViewById<Button>(R.id.btnSaveCompany)

        // Detectar si se está registrando o editando
        val companyId = intent.getIntExtra("COMPANY_ID", -1)
        val isEditing = companyId != -1

        // MODO EDICIÓN
        if (isEditing) {

            val company = databaseHelper.getCompanyById(companyId)

            if (company == null) {
                Toast.makeText(
                    this,
                    "No se encontró la empresa",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
                return
            }

            tvFormTitle.text = "Editar empresa"
            btnSave.text = "Actualizar empresa"

            // Cargar información existente
            etName.setText(company.name)
            etWebsite.setText(company.website)
            etPhone.setText(company.phone)
            etEmail.setText(company.email)
            etServices.setText(company.services)

            // Recuperar clasificaciones almacenadas en SQLite
            val savedCategories = company.classification
                .split(",")
                .map { it.trim() }

            cbConsulting.isChecked =
                "Consultoría" in savedCategories

            cbCustomDevelopment.isChecked =
                "Desarrollo a la medida" in savedCategories

            cbSoftwareFactory.isChecked =
                "Fábrica de software" in savedCategories

        } else {

            // MODO REGISTRO
            tvFormTitle.text = "Registrar empresa"
            btnSave.text = "Guardar empresa"
        }

        // GUARDAR O ACTUALIZAR EMPRESA
        btnSave.setOnClickListener {

            // Obtener datos del formulario
            val name = etName.text.toString().trim()
            val website = etWebsite.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val services = etServices.text.toString().trim()

            // Validar nombre obligatorio
            if (name.isEmpty()) {
                etName.error = "El nombre es obligatorio"
                etName.requestFocus()
                return@setOnClickListener
            }

            // Obtener clasificaciones seleccionadas
            val selectedCategories = mutableListOf<String>()

            if (cbConsulting.isChecked) {
                selectedCategories.add("Consultoría")
            }

            if (cbCustomDevelopment.isChecked) {
                selectedCategories.add("Desarrollo a la medida")
            }

            if (cbSoftwareFactory.isChecked) {
                selectedCategories.add("Fábrica de software")
            }

            // Validar que exista al menos una clasificación
            if (selectedCategories.isEmpty()) {
                Toast.makeText(
                    this,
                    "Selecciona al menos una clasificación",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Convertir lista de categorías en texto
            val classification = selectedCategories.joinToString(", ")

            // Crear objeto Company
            val company = Company(
                id = if (isEditing) companyId else 0,
                name = name,
                website = website,
                phone = phone,
                email = email,
                services = services,
                classification = classification
            )

            if (isEditing) {

                // UPDATE: actualizar empresa existente
                val rowsUpdated = databaseHelper.updateCompany(company)

                if (rowsUpdated > 0) {

                    Toast.makeText(
                        this,
                        "Empresa actualizada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "No se pudo actualizar la empresa",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {

                // CREATE: registrar empresa nueva
                val result = databaseHelper.insertCompany(company)

                if (result != -1L) {

                    Toast.makeText(
                        this,
                        "Empresa registrada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Error al guardar la empresa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Cerrar conexión con SQLite
    override fun onDestroy() {

        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }

        super.onDestroy()
    }
}
