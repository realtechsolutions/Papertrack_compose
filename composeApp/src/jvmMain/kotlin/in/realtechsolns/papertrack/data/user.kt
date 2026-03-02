package `in`.realtechsolns.papertrack.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.util.TableInfo

//@Entity
//data class User(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    val name: String
//)
@Entity  (indices = [Index(value = ["name"], unique = true)])
data class CompanyInfo (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
  val name: String,
    val address: String,
    val contactNo :String


)

@Entity
  //(indices = [Index(value = ["documentNo"]),Index(value = ["documentNo","revNumber"], unique = true)
//]
//)
data class DocumentRevision (
  @PrimaryKey (autoGenerate = true)
    val id: Long = 0,
    val documentNo: String,
    val revNumber: Int,
    //val revDate: Long = System.currentTimeMillis(),
    val revDate: String = "",
    val revReason : String ,
    val title : String,
  val fileName : String,
    val filePath : String

)

