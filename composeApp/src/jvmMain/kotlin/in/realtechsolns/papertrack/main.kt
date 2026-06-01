package `in`.realtechsolns.papertrack

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `in`.realtechsolns.papertrack.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import papertrack.composeapp.generated.resources.Res
import java.io.File
import java.util.zip.ZipInputStream

lateinit var db: AppDatabase
lateinit var companyDao: CompanyDao
lateinit var documentRevisionDao: DocumentRevisionDao
lateinit var documentsFolderDao: DocumentFolderDao
lateinit var documentSearchDa0: DocumentSearchDao
lateinit var contentSearchDao: ContentSearchDao
//val customLogoPath = mutableStateOf<String?>(null)
fun main() {

    //val title = mutableStateOf("Papertrack")

//    scope.launch {
//        db = getDatabaseBuilder()
//            .fallbackToDestructiveMigration(true)
//            .build()
//        companyDao = db.companyDao()
//        documentRevisionDao = db.documentRevisionDao()
//        documentsFolderDao = db.documentsFolderDao()
//        documentSearchDa0 = db.documentSearchDao()
//        contentSearchDao = db.contentSearchDao()
//        val existing = companyDao.getAll()
//        if (existing.isEmpty()) {
//            companyDao.insert(
//                CompanyInfo(
//                    name = "ABC Ltd",
//                    address = "999 Industrial area",
//                    contactNo = "99999"
//                )
//            )
//        }
//        copyFolderToUserSystem("Docs", "Docs")
//        copyFolderToUserSystem("orgChart", "orgChart")
//        title.value  = companyDao.getAll().first().name
//        //println(title.value)
//    }
    application {
        var isInitializing by remember { mutableStateOf(true) }
        val title = remember { mutableStateOf("Papertrack") }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                db = getDatabaseBuilder()
                    .fallbackToDestructiveMigration(true)
                    .build()
                companyDao = db.companyDao()
                // ... initialize other DAOs ...
                documentRevisionDao = db.documentRevisionDao()
                documentsFolderDao = db.documentsFolderDao()
                documentSearchDa0 = db.documentSearchDao()
                contentSearchDao = db.contentSearchDao()
                val existing = companyDao.getAll()
                if (existing.isEmpty()) {
                    companyDao.insert(
                        CompanyInfo(
                            name = "ABC Ltd",
                            address = "999 Industrial area",
                            contactNo = "99999"
                        )
                    )
                }

//                val existing = companyDao.getAll()
//                if (existing.isEmpty()) {
//                    companyDao.insert(CompanyInfo(name = "ABC Ltd", address = "999", contactNo = "999"))
//                }

                copyFolderToUserSystem("Docs", "Docs")
                copyFolderToUserSystem("orgChart", "orgChart")

                title.value = companyDao.getAll().first().name
            }
            // 3. Setup complete! Turn off the loader
            isInitializing = false
        }


        Window(
            onCloseRequest = ::exitApplication,
            title = title.value,
            //icon = painterResource(Res.drawable.papertrackcompanylogo)
            //icon = rememberAppLogo(customLogoPath.value)
        ) {
            var showHelp by remember { mutableStateOf(false) }
            var showLoader = remember { mutableStateOf(false) }

            AppMenuBar(
                onFolderOpen = { /* logic */ },
                onRefresh = { /* logic */ },
                onExit = ::exitApplication,
                onClick = { showHelp = !showHelp },
                showLoader = showLoader
            )
            Column {
                if (isInitializing) {
                  showLoader("...Loading Papertrack")
                } else {App()}

                //App()
                if (showLoader.value) {
                    showLoader(".... Loading documents for search")
                }
            }
            if (showHelp) {showHelp {showHelp =!showHelp } }

        }
    }
}

suspend fun copyFolderToUserSystem(folderName: String, targetSubPath: String) {
    val userHome = System.getProperty("user.home")
    //val targetDir = File(userHome, targetSubPath)
    val targetDir = File(getDocumentDir(), targetSubPath)
    //val nestedDir = File(targetDir, "Docs")
    if (targetDir.exists() && targetDir.list()?.isNotEmpty() == true) {
        return // Files are already there, skip the unzip!
    }
    if (!targetDir.exists()) targetDir.mkdirs()

    try {
//        if (nestedDir.exists() && nestedDir.list()?.isNotEmpty() == true) {
//            return
      //  }
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

    } catch (e: Exception) {
        e.printStackTrace()
    }
}