
package co.edu.unal.softwaredirectory

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var etSearch: EditText
    private lateinit var spClassification: Spinner
    private lateinit var btnAddCompany: Button
    private lateinit var lvCompanies: ListView
    private lateinit var tvResults: TextView

    private var displayedCompanies: List<Company> = emptyList()

    // Clasificaciones solicitadas en el Reto 8
    private val classifications = listOf(
        "Todas las clasificaciones",
        "Consultoría",
        "Desarrollo a la medida",
        "Fábrica de software"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        etSearch = findViewById(R.id.etSearch)
        spClassification = findViewById(R.id.spClassification)
        btnAddCompany = findViewById(R.id.btnAddCompany)
        lvCompanies = findViewById(R.id.lvCompanies)
        tvResults = findViewById(R.id.tvResults)

        setupClassificationSpinner()
        setupSearch()
        setupButtons()
        loadCompanies()
    }

    // Configurar filtro por clasificación
    private fun setupClassificationSpinner() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            classifications
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spClassification.adapter = adapter

        spClassification.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    loadCompanies()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }
    }

    // Buscar empresas por nombre
    private fun setupSearch() {

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                loadCompanies()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    // Configurar acciones principales
    private fun setupButtons() {

        // Registrar empresa
        btnAddCompany.setOnClickListener {

            val intent = Intent(
                this,
                CompanyFormActivity::class.java
            )

            startActivity(intent)
        }

        // Consultar empresa seleccionada
        lvCompanies.setOnItemClickListener { _, _, position, _ ->

            val company = displayedCompanies[position]

            showCompanyOptions(company)
        }
    }

    // Mostrar datos de la empresa
    private fun showCompanyOptions(company: Company) {

        val message = """
            Clasificación:
            ${company.classification}

            Página web:
            ${company.website}

            Teléfono:
            ${company.phone}

            Correo electrónico:
            ${company.email}

            Productos y servicios:
            ${company.services}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(company.name)
            .setMessage(message)
            .setPositiveButton("Editar") { _, _ ->
                editCompany(company)
            }
            .setNegativeButton("Cerrar", null)
            .setNeutralButton("Eliminar") { _, _ ->
                confirmDelete(company)
            }
            .show()
    }

    // Editar empresa
    private fun editCompany(company: Company) {

        val intent = Intent(
            this,
            CompanyFormActivity::class.java
        )

        intent.putExtra("COMPANY_ID", company.id)

        startActivity(intent)
    }

    // Confirmar eliminación
    private fun confirmDelete(company: Company) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar empresa")
            .setMessage(
                "¿Estás seguro de eliminar ${company.name}? " +
                        "Esta acción no se puede deshacer."
            )
            .setPositiveButton("Eliminar") { _, _ ->

                val rowsDeleted =
                    databaseHelper.deleteCompany(company.id)

                if (rowsDeleted > 0) {

                    Toast.makeText(
                        this,
                        "Empresa eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadCompanies()

                } else {

                    Toast.makeText(
                        this,
                        "No se pudo eliminar la empresa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Consultar empresas aplicando filtros
    private fun loadCompanies() {

        val searchText = etSearch.text.toString().trim()

        val selectedClassification =
            spClassification.selectedItem?.toString() ?: ""

        val classificationFilter =
            if (
                selectedClassification.isEmpty() ||
                selectedClassification == "Todas las clasificaciones"
            ) {
                ""
            } else {
                selectedClassification
            }

        displayedCompanies = databaseHelper.searchCompanies(
            name = searchText,
            classification = classificationFilter
        )

        // Mostrar empresas y sus clasificaciones
        val companyNames = displayedCompanies.map { company ->

            "${company.name}\n${company.classification}"
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            companyNames
        )

        lvCompanies.adapter = adapter

        tvResults.text =
            "Empresas encontradas (${displayedCompanies.size})"
    }

    // Recargar lista después de registrar o editar
    override fun onResume() {
        super.onResume()

        if (::databaseHelper.isInitialized) {
            loadCompanies()
        }
    }

    // Cerrar conexión SQLite
    override fun onDestroy() {

        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }

        super.onDestroy()
    }
}

