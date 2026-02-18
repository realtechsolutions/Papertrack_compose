package `in`.realtechsolns.papertrack

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.realtechsolns.papertrack.data.CompanyInfo
import `in`.realtechsolns.papertrack.data.User
import `in`.realtechsolns.papertrack.data.getDatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import papertrack.composeapp.generated.resources.Res
import java.io.File
import java.util.zip.ZipInputStream


fun main() {

    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        val db = getDatabaseBuilder()
            .fallbackToDestructiveMigration(false)
            .build()

        val dao = db.userDao()
        val companyDao = db.companyDao()

        run {
            companyDao.insert(CompanyInfo(name = "ABC Ltd", address = "999 Industrial area", contactNo = "99999"))
            val company  = companyDao.getAll()
            println(company)
        }

        copyDocsUserSystem()
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "papertrack",
        ) {

            App()
        }
    }
}

suspend fun copyDocsUserSystem() {
    val userHome = System.getProperty("user.home")
    val targetDir = File(userHome, "Papertracks/Docs")

    // Create directory if it doesn't exist
    if (!targetDir.exists()) targetDir.mkdirs()
    try {
        val bytes = Res.readBytes("files/Docs.zip")
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    // Write to the USER directory (this is safe)
                    newFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        println("Deployment successful to: ${targetDir.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()

    }
}