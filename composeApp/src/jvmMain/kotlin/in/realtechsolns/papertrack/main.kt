package `in`.realtechsolns.papertrack

//import `in`.realtechsolns.papertrack.data.User

import androidx.compose.foundation.Image
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.realtechsolns.papertrack.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import papertrack.composeapp.generated.resources.Res
import papertrack.composeapp.generated.resources.papertrackcompanylogo
import java.io.File
import java.util.zip.ZipInputStream

//lateinit var prefs: Preferences
lateinit var db: AppDatabase
lateinit var companyDao: CompanyDao
lateinit var documentRevisionDao : DocumentRevisionDao
lateinit var documentsFolderDao : DocumentFolderDao
lateinit var documentSearchDa0 : DocumentSearchDao
lateinit var contentSearchDa : ContentSearchDao
fun main() {

    val scope = CoroutineScope(Dispatchers.IO)
    val title = mutableStateOf("Papertrack")
    scope.launch {
        db = getDatabaseBuilder()
            .fallbackToDestructiveMigration(true)
            .build()

         companyDao = db.companyDao()
        documentRevisionDao = db.documentRevisionDao()
        documentsFolderDao = db.documentsFolderDao()
        documentSearchDa0 = db.documentSearchDao()
        contentSearchDa = db.contentSearchDao()

            //companyDao.insert(CompanyInfo(name = "ABC Ltd", address = "999 Industrial area", contactNo = "99999"))
            //val company  = companyDao.getAll()
           // println(company)
        copyFolderToUserSystem("Docs","Papertracks/Docs")
        copyFolderToUserSystem("orgChart","Papertracks/orgChart")
        //title.value = companyDao.getAll().first().name
        //copyDocsUserSystem()
    }

    //prefs = Preferences.userRoot().node("MyAppPreferences")
    //LuceneManager.initialize()


    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Papertrack",
            icon = painterResource(Res.drawable.papertrackcompanylogo)
        ) {
               AppMenuBar(
                   onFolderOpen = { /* logic */ },
                   onRefresh = { /* logic */ },
                   onExit = ::exitApplication
               )
            App()
        }
    }
}


suspend fun copyFolderToUserSystem(folderName: String, targetSubPath: String) {
    val userHome = System.getProperty("user.home")
    val targetDir = File(userHome, targetSubPath)
    val nestedDir = File(targetDir, "Docs")

    if (!targetDir.exists()) targetDir.mkdirs()

    try {
        if (nestedDir.exists() && nestedDir.list()?.isNotEmpty() == true) {
            return
        }
        val bytes = Res.readBytes("files/$folderName.zip")
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
       // println("Deployment successful to: ${targetDir.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}