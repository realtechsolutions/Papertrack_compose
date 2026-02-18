package `in`.realtechsolns.papertrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
@Entity
data class CompanyInfo (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
  val name: String,
    val address: String,
    val contactNo :String


)