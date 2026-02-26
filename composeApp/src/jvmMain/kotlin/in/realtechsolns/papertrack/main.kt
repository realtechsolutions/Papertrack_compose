package `in`.realtechsolns.papertrack

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Database
import `in`.realtechsolns.papertrack.data.AppDatabase
import `in`.realtechsolns.papertrack.data.CompanyDao
import `in`.realtechsolns.papertrack.data.CompanyInfo
import `in`.realtechsolns.papertrack.data.DocumentRevisionDao
//import `in`.realtechsolns.papertrack.data.User
import `in`.realtechsolns.papertrack.data.getDatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import papertrack.composeapp.generated.resources.Res
import java.awt.BorderLayout
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.prefs.Preferences
import java.util.zip.ZipInputStream
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

lateinit var prefs: Preferences
lateinit var db: AppDatabase
lateinit var companyDao: CompanyDao
lateinit var documentRevisionDao : DocumentRevisionDao
fun main() {

    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        db = getDatabaseBuilder()
            .fallbackToDestructiveMigration(true)
            .build()

         companyDao = db.companyDao()
        documentRevisionDao = db.documentRevisionDao()
        run {
            companyDao.insert(CompanyInfo(name = "ABC Ltd", address = "999 Industrial area", contactNo = "99999"))
            val company  = companyDao.getAll()
            println(company)
        }
        copyFolderToUserSystem("Docs","Papertracks/Docs")
        copyFolderToUserSystem("orgChart","Papertracks/orgChart")

        //copyDocsUserSystem()
    }

    prefs = Preferences.userRoot().node("MyAppPreferences")
    LuceneManager.initialize()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Papertrack",
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

    if (!targetDir.exists()) targetDir.mkdirs()
    try {
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