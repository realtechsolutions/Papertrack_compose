package `in`.realtechsolns.papertrack


import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import `in`.realtechsolns.papertrack.data.DocumentRevision
import `in`.realtechsolns.papertrack.data.DocumentRevisionDao
import `in`.realtechsolns.papertrack.data.DocumentSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.awt.Desktop
import java.io.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.JOptionPane

val userHome: String? = System.getProperty("user.home")
var folder = mutableStateOf(File(userHome, "Papertracks/Docs/Docs"))
val orgChart: File = File(userHome, "Papertracks/orgChart/orgChart/index.html")
val editOrgChart: File = File(userHome, "Papertracks/orgChart/orgChart/edit.html")
val desktop: Desktop? = Desktop.getDesktop()
var isRevHistoryVisible = mutableStateOf(false)
var isPreviousVersionVisible = mutableStateOf(false)
var currentFileName = mutableStateOf("")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileTreeItem(file: File, initialExpanded: Boolean = false, dao: DocumentRevisionDao = documentRevisionDao) {
    var showFileUpdateLoader = remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(start = 16.dp)) {
        var isMenuVisible by remember { mutableStateOf(false) }
        CursorDropdownMenu(
            expanded = isMenuVisible,
            onDismissRequest = { isMenuVisible = false }
        ) {
            DropdownMenuItem(
                text = { Text("Update File") },
                onClick = { /* handle */ isMenuVisible = false
                    updateFile(file, showLoader = showFileUpdateLoader, scope = scope)
                }
            )
            DropdownMenuItem(
                text = { Text("View revision history") },
                onClick = {
                    isMenuVisible = false
                    scope.launch {
                        isRevHistoryVisible.value = !isRevHistoryVisible.value
                        val revHistory = dao.getFullRevisionHistory(file.name).first()
                        // dao.getFullRevisionHistory(file.name)
                        println(revHistory)
                        currentFileName.value = file.name
                        revHistory.forEach {
                            println(" Rev. No. :${it.revNumber} Rev. Date: ${it.revDate} Revision Reason :${it.revReason}")

                        }

                    }
                }
            )
            DropdownMenuItem(
                text = { Text("View previous versions") },
                onClick = { /* handle */ isMenuVisible = false
                    isPreviousVersionVisible.value = !isPreviousVersionVisible.value
                    currentFileName.value = file.name
                }
            )
        }

        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        if (file.isDirectory) {
                            isExpanded = !isExpanded
                        }
                    },
                    onDoubleClick = {
                        if (file.isFile) {
                            try {
                                Desktop.getDesktop().open(file)
                            } catch (e: Exception) {
                                println("Could not open file: ${e.message}")
                            }
                        }
                    }
                )
                .onPointerEvent(PointerEventType.Press) {
                    if (it.buttons.isSecondaryPressed) {

                        if (file.isFile) {
                            isMenuVisible = !isMenuVisible
                            // currentFileName = file.name
                        }
                    }
                }

                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Simple logic: If it's a directory, show a folder icon; else a file icon.
            Text(if (file.isDirectory) "📁 " else "📄 ")
            val displayName = if (file.isDirectory) {
                file.name
            } else {
                file.name.substringBeforeLast(".", missingDelimiterValue = file.name)
            }
            Text(displayName)
        }

        // 4. THE RECURSION (THE "DEEP DIVE")
        // This ONLY runs if the user clicked the folder (isExpanded == true).
        if (isExpanded && file.isDirectory) {

            // Look inside the physical folder on your hard drive
            val children = file.listFiles()
            children?.forEach { child ->
                // This is the magic part: The function calls ITSELF
                // for every file it found inside.
                FileTreeItem(child)
            }
        }
        if (showFileUpdateLoader.value) {

            showLoader(".... Updating file ")
        }
    }
}

private fun updateFile(
    file: File, dao: DocumentRevisionDao = documentRevisionDao, showLoader: MutableState<Boolean>,
    scope: CoroutineScope

) {
    try {
        RandomAccessFile(file, "rw").close()
    } catch (e: IOException) {
        JOptionPane.showMessageDialog(
            null,
            "Please close the file before updating.",
            "File Open",
            JOptionPane.WARNING_MESSAGE
        )
        return
    }
    val reason = JOptionPane.showInputDialog(null, "Enter reason for revision")
    showLoader.value = true
    val revNumberRegex = Regex("Revision [Nn]umber: (\\d+)")
    val revDateRegex = Regex("Revision Date: (\\d{2}[/-]\\d{2}[/-]\\d{4})")
    var newRevNumber = 1
    var currentRevNumber = 1
    val titleRegex = Regex("Title:\\s*(.+)")
    val documentNoRegex = Regex("Document No\\.?\\s*:?\\s*(.+)")
    var extractedTitle = ""
    var extractedDocumentNo = ""
    var extractedRevDate: String = ""
    scope.launch(Dispatchers.IO) {
// ✅ Set name first
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val timestamp = LocalDateTime.now().format(formatter)
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val obsoleteFileName = "${file.nameWithoutExtension}_$timestamp.docx"
        val obsoleteFile = File(userHome, "obsoleteDocs/$obsoleteFileName")
        try {
            file.copyTo(obsoleteFile, overwrite = true)
            println("✅ copy succeeded, size = ${obsoleteFile.length()}")
        } catch (e: Exception) {
            println("❌ copy FAILED: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
        }

        FileInputStream(file).use { inStream ->
            val doc = XWPFDocument(inStream)
            val header = doc.headerFooterPolicy?.defaultHeader ?: return@launch
            for (paragraph in header.paragraphs) {
                val fullText = paragraph.text // Simpler way to get text
                revNumberRegex.find(fullText)?.let { currentRevNumber = it.groupValues[1].toIntOrNull() ?: 0 }
                titleRegex.find(fullText)?.let { extractedTitle = it.groupValues[1].trim() }
                documentNoRegex.find(fullText)?.let { extractedDocumentNo = it.groupValues[1].trim() }
                // CAPTURE THE OLD DATE HERE BEFORE REPLACING IT
                revDateRegex.find(fullText)?.let { extractedRevDate = it.groupValues[1].trim() }
            }
            newRevNumber = currentRevNumber + 1

// 2. SECOND PASS: Update the header with NEW info
            for (paragraph in header.paragraphs) {
                val runs = paragraph.runs
                if (runs.isEmpty()) continue
                val fullText = runs.joinToString("") { it.text() }
                if (fullText.contains("Revision", ignoreCase = true)) {
                    var updatedText = revDateRegex.replace(fullText, "Revision Date: $currentDate")
                    updatedText =
                        revNumberRegex.replace(updatedText, "Revision Number: ${String.format("%02d", newRevNumber)}")

                    runs[0].setText(updatedText, 0)
                    for (i in 1 until runs.size) runs[i].setText("", 0)
                }
            }

            FileOutputStream(file).use { outStream ->
                doc.write(outStream)
                doc.close()
            }
        }
        // 2️⃣ Insert into DB
        // scope.launch {
        dao.insertRevisionInternal(
            DocumentRevision(
                documentNo = extractedDocumentNo,
                revNumber = currentRevNumber,
                revReason = reason,
                title = extractedTitle,
                fileName = file.name,
                filePath = "$userHome/obsoleteDocs/$obsoleteFileName",
                revDate = extractedRevDate
            )
        )


        val filesToDelete = dao.getFilePathsToDelete(file.name)
               filesToDelete.forEach {
            val file = File(it)
            if (file.exists()) file.delete()
        }
        showLoader.value = false
    }

    JOptionPane.showMessageDialog(
        null, "file updated" +
                "with  rev no: $newRevNumber , reason: $reason . Old version also saved "
    )
    // }
}

@Composable
fun showRevisionHistory(dao: DocumentRevisionDao = documentRevisionDao, filename: String) {
    val scope = rememberCoroutineScope()
    // 1. Force the Flow to re-bind whenever the filename changes
    val revHistory by remember(filename) {
        dao.getFullRevisionHistory(filename)
    }.collectAsState(initial = emptyList())

    if (isRevHistoryVisible.value) {
        DialogWindow(
            onCloseRequest = { isRevHistoryVisible.value = false },
            title = "History for $filename"
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {

                // --- DEBUG SECTION ---
                LaunchedEffect(filename) {
                    //println("UI is querying for: '$filename'")
                }

                if (revHistory.isEmpty()) {
                    Text("No history for '$filename'")

                    // Button to force a manual check
                    Button(onClick = {
                        scope.launch {
                            val check = dao.getFullRevisionHistory(filename).first()
                            println("Manual Check within UI: Found ${check.size} items for '$filename'")
                        }
                    }) {
                        Text("Force Manual Check")
                    }
                } else {
                    LazyColumn {
                        items(revHistory) { item ->
                            Text("Rev. No. ${item.revNumber}  Rev. Date: ${item.revDate} Rev. reason : ${item.revReason} ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun showPreviousVersions(dao: DocumentRevisionDao = documentRevisionDao, filename: String) {
    val scope = rememberCoroutineScope()
    // 1. Force the Flow to re-bind whenever the filename changes
    val last3Versions by remember(filename) {
        dao.getLast3Versions(filename)
    }.collectAsState(initial = emptyList())

    if (isPreviousVersionVisible.value) {
        DialogWindow(
            onCloseRequest = { isPreviousVersionVisible.value = false },
            title = "View Last 3 versions  for $filename"
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {

                // --- DEBUG SECTION ---
                LaunchedEffect(filename) {
                    // println("UI is querying for: '$filename'")
                }

                if (last3Versions.isEmpty()) {
                    Text("No history for '$filename'")

                    // Button to force a manual check
                    Button(onClick = {
                        scope.launch {
                            val check = dao.getFullRevisionHistory(filename).first()
                            println("Manual Check within UI: Found ${check.size} items for '$filename'")
                        }
                    }) {
                        Text("Force Manual Check")
                    }
                } else {
                    LazyColumn {
                        items(last3Versions) { item ->
                            Text(
                                text = "Rev. No. ${item.revNumber}  Rev. Date: ${item.revDate} Rev. reason : ${item.revReason}",
                                modifier = Modifier.clickable(onClick = {
                                    val f = File(item.filePath)
                                    Desktop.getDesktop().open(f)
                                })
                            )
                        }
                    }
                }
            }
        }
    }
}


//fun openFilePicker(): String? {
//    val fileChooser = JFileChooser()
//    fileChooser.dialogTitle = "Select logo Image"
//    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
//    val result = fileChooser.showOpenDialog(null)
//    return if (result == JFileChooser.APPROVE_OPTION) {
//        fileChooser.selectedFile.absolutePath
//    } else {
//        null
//    }
//}

fun openFolderPicker(): String? {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Select your documents folder"
    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val result = fileChooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.absolutePath
    } else {
        null
    }
}

fun getAllDocxContents(folderPath: String): List<DocumentSearch> {
    val result = mutableListOf<DocumentSearch>()
    val root = File(folderPath)
    if (!root.exists()) return emptyList()
    root.walkTopDown().forEach { file ->
        if (file.isFile && file.extension.equals("docx", ignoreCase = true)) {
            val text = extractTextFromDocx(file)
            val headerText = getRawHeaderText(file)
            //println(headerText)
            val revisionNumber = findValue(headerText, "Revision Number:")
            val revisionDate = findValue(headerText, "Revision Date:")
            val docNo = findValue(headerText, "Document No:")
            val title = findValue(headerText, "Title:")
            println(docNo)
            if (text.isNotBlank()) {
                val listItem = DocumentSearch(
                    content = text, filePath = file.absolutePath,
                    documentNo = docNo,
                    revDate = revisionDate,
                    // revNo = revisionNumber.toInt(),
                    revNo = revisionNumber.toIntOrNull() ?: 0,
                    title = title
                )
                result.add(listItem)
            }
        }
    }
    return result
}


fun extractTextFromDocx(file: File): String {
    return try {
        XWPFDocument(file.inputStream()).use { document ->
            val paragraphs = document.paragraphs
            val text = StringBuilder()
            for (para in paragraphs) {
                text.append(para.text).append("\n")
            }
            text.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

@Composable
fun FileListTextSearch(filePaths: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(filePaths) { filePath ->
            val file = File(filePath)
            Text(
                text = file.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable(onClick = {
                        desktop?.open(file)
                    }),
                style = MaterialTheme.typography.body2
            )
            HorizontalDivider() // Adds a thin line between items
        }
    }
}

fun getRawHeaderText(file: java.io.File): String {
    return try {
        org.apache.poi.xwpf.usermodel.XWPFDocument(file.inputStream()).use { doc ->
            val sb = StringBuilder()
            // This grabs ALL headers defined in the document (Default, First Page, Even)
            for (header in doc.headerList) {
                for (para in header.paragraphs) {
                    // Using para.text automatically joins all "Runs" correctly
                    sb.append(para.text).append("\n")
                }
            }
            sb.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun findValue(headerText: String, label: String): String {
    if (headerText.isBlank()) return "Header Empty"
    // 1. We replace the specific space (\s) with a wider range [\s\u00A0]
    // to catch "Non-breaking spaces" common in Word.
    // 2. We allow for any character that looks like a colon [:：]
    // 3. We use [^\n\r\t]{2,} to detect the "Gap" between fields
    val regex = "(?i)$label\\.?[:：\\s]*\\s*(.*?)(?=\\s{2,}|\u00A0{2,}|[\\r\\n\\t]|$)".toRegex()
    val match = regex.find(headerText)
    return match?.groupValues?.get(1)?.trim() ?: "Not Found"
}

@Composable
fun showLoader(name: String) {
    Column {
        CircularProgressIndicator()
        Text(name)
    }
}


@Composable
fun showHelp(click: () -> Unit) {
    DialogWindow(
        onCloseRequest = { click() },
        title = "Help"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(" Add your documents  folder in document menu, click Add  your  documents ")
            HorizontalDivider()
            Text("Add documents to search' in search menu by clicking Add documents to search ")
            HorizontalDivider()
            Text(" Documents must have a header with Revision Number, Revision Date,Document No , and Title ")
            HorizontalDivider()
            Text(" Documents  folder is shown as a tree view you can open file by clicking it .  ")
            HorizontalDivider()
            Text(" You can auto update revision number and revision date by right clicking file and update file.")
            HorizontalDivider()
            Text(" You can view revision history and can view previous three revisions. ")
            HorizontalDivider()
        }
    }
}





