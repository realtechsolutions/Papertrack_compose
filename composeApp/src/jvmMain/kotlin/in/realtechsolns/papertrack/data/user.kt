package `in`.realtechsolns.papertrack.data

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.util.TableInfo
import `in`.realtechsolns.papertrack.userHome
import java.io.File

@Entity
data class DocumentsFolder(
    @PrimaryKey
    val id: Int = 1,
    val userFolder: String,
    val defaultFolderPath : String = File(userHome, "Papertracks/Docs/Docs").absolutePath

)

@Entity  (indices = [Index(value = ["name"], unique = true)])
data class CompanyInfo (
    @PrimaryKey
    val id: Int = 1,
  val name: String = "ABC Ltd.",
    val address: String = " 99 ,Industrial Area XYZ",
    val contactNo :String = "9999999"
)

@Entity
  (indices = [Index(value = ["documentNo"]),Index(value = ["documentNo","revNumber"], unique = true)
]
)
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

@Entity
data class DocumentSearch (
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val documentNo: String = "",
    val revNo : Int = 0,
    val revDate  : String = "",
    val fileName : String  = "",
    val content : String = ""
   )

@Entity
@Fts4(contentEntity = DocumentSearch ::class)
data class  ContentSearch (
    val content : String =""

)

