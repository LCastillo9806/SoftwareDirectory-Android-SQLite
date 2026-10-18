
package co.edu.unal.softwaredirectory

data class Company(
    val id: Int = 0,
    val name: String,
    val website: String,
    val phone: String,
    val email: String,
    val services: String,
    val classification: String
)
