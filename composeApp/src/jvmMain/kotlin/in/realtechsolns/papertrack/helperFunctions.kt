package `in`.realtechsolns.papertrack


import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import `in`.realtechsolns.papertrack.data.DocumentRevision
import `in`.realtechsolns.papertrack.data.DocumentRevisionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.math3.ode.MainStateJacobianProvider
import org.apache.poi.hpsf.Date
import org.apache.poi.util.Units
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFFooter
import org.apache.poi.xwpf.usermodel.XWPFHeader
import java.awt.Desktop
import java.awt.GridLayout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.tree.DefaultMutableTreeNode
val userHome: String? = System.getProperty("user.home")
var folder: File = File(userHome, "Papertracks/Docs/Docs")
val orgChart :File = File(userHome, "Papertracks/orgChart/orgChart/index.html")
val editOrgChart :File = File(userHome, "Papertracks/orgChart/orgChart/edit.html")

val desktop: Desktop? = Desktop.getDesktop()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileTreeItem(file: File, initialExpanded: Boolean = false) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    Column(modifier = Modifier.padding(start = 16.dp)) {
        var isMenuVisible by remember { mutableStateOf(false) }
        CursorDropdownMenu(
            expanded = isMenuVisible,
            onDismissRequest = { isMenuVisible = false }
        ) {
            DropdownMenuItem(
                text = { Text("Update File") },
                onClick = { /* handle */ isMenuVisible = false
                    println("${file.name}  Updated")
                    updateFile(file)
                }
            )
            DropdownMenuItem(
                text = { Text("Revision History") },
                onClick = { /* handle */ isMenuVisible = false }
            )
            DropdownMenuItem(
                text = { Text("Previous Versions") },
                onClick = { /* handle */ isMenuVisible = false }
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
    }
}

//private fun updateFile(
//    originalFile: File,
//    //documentNo: String,
//    //title: String,
//    revReason: String,
//    userDirectory: File,
//    dao: DocumentRevisionDao
//) {
//
//    val currentDate = LocalDate.now()
//        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//
//    try {
//        RandomAccessFile(originalFile, "rw").close()
//    } catch (e: IOException) {
//        JOptionPane.showMessageDialog(
//            null,
//            "Please close the file before updating.",
//            "File Open",
//            JOptionPane.WARNING_MESSAGE
//        )
//        return
//    }
//
//    val revDateRegex = Regex("Revision Date: \\d{2}/\\d{2}/\\d{4}")
//    val revNumberRegex = Regex("Revision [Nn]umber: (\\d+)")
//    val titleRegex = Regex("Title:\\s*(.+)")
//    val documentNoRegex = Regex("Document No\\.?\\s*:?\\s*(.+)")
//    var newRevNumber = 1
//    var extractedTitle = ""
//    var extractedDocumentNo = ""
//    val updatedBytes: ByteArray
//
//    FileInputStream(originalFile).use { inStream ->
//        val doc = XWPFDocument(inStream)
//        val header = doc.headerFooterPolicy?.defaultHeader ?: return
//
//        for (paragraph in header.paragraphs) {
//            val runs = paragraph.runs
//            if (runs.isEmpty()) continue
//
//            val fullText = runs.joinToString("") { it.text() }
//            val titleMatch = titleRegex.find(fullText)
//            if (titleMatch != null) {
//                extractedTitle = titleMatch.groupValues[1].trim()
//            }
//
//            // -------- Extract Document No --------
//            val docMatch = documentNoRegex.find(fullText)
//            if (docMatch != null) {
//                extractedDocumentNo = docMatch.groupValues[1].trim()
//            }
//
//
//
//            if (!fullText.contains("Revision", ignoreCase = true)) continue
//
//            val revMatch = revNumberRegex.find(fullText)
//            val currentRev = revMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
//
//
//
//            newRevNumber = currentRev + 1
//
//            var updatedText =
//                revDateRegex.replace(fullText, "Revision Date: $currentDate")
//
//            updatedText =
//                revNumberRegex.replace(
//                    updatedText,
//                    "Revision Number: ${String.format("%02d", newRevNumber)}"
//                )
//
//            runs[0].setText(updatedText, 0)
//            for (i in 1 until runs.size) {
//                runs[i].setText("", 0)
//            }
//        }
//
//        ByteArrayOutputStream().use { baos ->
//            doc.write(baos)
//            updatedBytes = baos.toByteArray()
//        }
//    }
//
//    // 🔥 Now do DB + File saving in background
//    CoroutineScope(Dispatchers.IO).launch {
//
//        if (!userDirectory.exists()) {
//            userDirectory.mkdirs()
//        }
//
//        val nextRev = dao.getNextRevisionNumber(documentNo)
//
//        val newFileName = "${documentNo}_rev${String.format("%02d", nextRev)}.docx"
//        val newFile = File(userDirectory, newFileName)
//
//        try {
//            // 1️⃣ Save file
//            newFile.writeBytes(updatedBytes)
//
//            // 2️⃣ Insert into DB
//            dao.insertRevisionInternal(
//                DocumentRevision(
//                    documentNo = extractedDocumentNo,
//                    revNumber = currentRev ,
//                    revReason = revReason,
//                    title = title,
//                    filePath = originalFile.path
//                )
//            )
//
//            // 3️⃣ Delete older revisions from DB
//            dao.deleteOlderRevisions(documentNo)
//
//            // 4️⃣ Delete old files physically
//            val revisions = dao.getRevisionsOfDocument(documentNo)
//            if (revisions.size > 5) {
//                val toDelete = revisions.drop(5)
//                toDelete.forEach {
//                    File(it.filePath).delete()
//                }
//            }
//
//        } catch (e: Exception) {
//            if (newFile.exists()) newFile.delete()
//            e.printStackTrace()
//        }
//    }
//
//    println("Revision $newRevNumber saved successfully")
//}
//
//


private fun updateFile(file: File, dao: DocumentRevisionDao = documentRevisionDao) {

    val scope = CoroutineScope(Dispatchers.Default)
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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
    //val revDateRegex = Regex("Revision Date: \\d{2}/\\d{2}/\\d{4}")
    val revDateRegex = Regex("Revision Date: (\\d{2}/\\d{2}/\\d{4})")
    val revNumberRegex = Regex("Revision [Nn]umber: (\\d+)")
    var newRevNumber = 1
    var currentRevNumber = 1
    val titleRegex = Regex("Title:\\s*(.+)")
   val documentNoRegex = Regex("Document No\\.?\\s*:?\\s*(.+)")
   //var newRevNumber = 1
   var extractedTitle = ""
   var extractedDocumentNo = ""
    var obsoleteFileName:String =""
    var obsoleteFile :File
    var extractedRevDate :String = ""

    FileInputStream(file).use { inStream ->
        val doc = XWPFDocument(inStream)
        val header = doc.headerFooterPolicy?.defaultHeader ?: return
        for (paragraph in header.paragraphs) {
            val runs = paragraph.runs
            if (runs.isEmpty()) continue
            val fullText = runs.joinToString("") { it.text() }
            if (!fullText.contains("Revision", ignoreCase = true)) continue
            val revMatch = revNumberRegex.find(fullText)
            val currentRev = revMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            currentRevNumber = currentRev
            newRevNumber = currentRev + 1
            val titleMatch = titleRegex.find(fullText)
            if (titleMatch != null) { extractedTitle = titleMatch.groupValues[1].trim()
            }

            // -------- Extract Document No --------
            val docMatch = documentNoRegex.find(fullText)
            if (docMatch != null) {
                extractedDocumentNo = docMatch.groupValues[1].trim()
            }
            val revDateMatch = revDateRegex.find(fullText)
            if (revDateMatch != null) {extractedRevDate = revDateMatch.groupValues[1].trim()}

             obsoleteFileName = "${extractedTitle}_rev${String.format("%02d", currentRevNumber)}.docx"
             obsoleteFile= File(userHome, "obsoleteDocs/$obsoleteFileName")
            obsoleteFile.parentFile?.mkdirs()
            FileOutputStream(obsoleteFile).use { outStream ->doc.write(outStream) }
            var updatedText = revDateRegex.replace(fullText, "Revision Date: $currentDate")
            updatedText = revNumberRegex.replace(updatedText, "Revision Number: ${String.format("%02d", newRevNumber)}")

            runs[0].setText(updatedText, 0)
            for (i in 1 until runs.size) {
                runs[i].setText("", 0)
            }
        }

        FileOutputStream(file).use { outStream ->
            doc.write(outStream)
        }
    }
                // 2️⃣ Insert into DB
    scope.launch {

        dao.insertRevisionInternal(
            DocumentRevision(
                documentNo = extractedDocumentNo,
                revNumber = currentRevNumber,
                revReason = reason,
                title = extractedTitle,
                filePath = "$userHome/obsoleteDocs/$obsoleteFileName",
                revDate = extractedRevDate
            )
        )
    }

    JOptionPane.showMessageDialog(null, "file updated" +
            "with  rev no: $newRevNumber , reason: $reason . Old version also saved ")



}


fun openFile(name: String, folder: File) {
    val list = folder.listFiles()
    for (file in list) {
        var filename = file.name
        filename = if ((filename.indexOf(".") > 0)) filename.substring(0, filename.lastIndexOf(".")) else filename
        if (file.isDirectory) {
            openFile(name, file)
        } else if (name.equals(filename, ignoreCase = true)) try {
            desktop?.open(file)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}

fun openFolder(name: String, folder: File) {
    val list = folder.listFiles()
    for (file in list) {
        val fileName = file.name
        if (name.equals(fileName, ignoreCase = true)) {
            try {
                desktop?.open(file)
            } catch (e: IOException) {
            }
        } else {
            if (file.isDirectory) {
                openFolder(name, file)
            }
        }
    }
}

fun delete(fileName: String, folder: File) {
    val list = folder.listFiles()
    for (file in list) {
        if (fileName.equals(file.name.substringBeforeLast('.'), ignoreCase = true))
            file.delete()
        else {
            if (file.isDirectory)
                delete(fileName, file)
        }
    }
}

// this functions takes a strings for folder name and file names and also takes a folder of type File
// it the creates a list of file in top folder and add file to folder name folder in top folder
// through a recurssive call
fun addFile(folderName: String, fileName: String, topFolder: File? = folder) {
    val list = topFolder?.listFiles() ?: return
    for (file in list) {
        if (file.isDirectory && folderName.equals(file.name, ignoreCase = true)) {
            createFile(file, fileName)
            return // Exit the function after finding the folder
        } else if (file.isDirectory) {
            addFile(folderName, fileName, file) // Recursively search in subdirectories
        } else if (file.isFile) {
            println("add to folder")
        }
    }
}

//this function takes a folder and file name string and creates a word doc object . It creates a file in folder
// given as parameter in create File function and the create output stream of this file and finally write
// doc to this file
fun createFile(folder: File, fileName: String) {
    val doc: XWPFDocument = XWPFDocument()
    println("createFile function called")
    val docInfo = getUserInputs("Document title", "Document No.", "Revision Number", "Revision Date")
    val headerFooterPolicy = doc.createHeaderFooterPolicy()
    val header: XWPFHeader = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT)
    val footer: XWPFFooter = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT)
    val headerContent = header.createParagraph()
    val headerRun = headerContent.createRun()
    val footerContent = footer.createParagraph()
    val footerRun = footerContent.createRun()
    val image: FileInputStream = FileInputStream(
        prefs.get(
            "logoImage",
            ""
        ).toString()
    )
    headerRun.addPicture(image, XWPFDocument.PICTURE_TYPE_PNG, "img", Units.toEMU(30.0), Units.toEMU(30.0))
    val companyName = prefs.get("companyName", "ABC Ltd")
    headerRun.setText(companyName)
    headerRun.addBreak()
    headerRun.setText("Document No: ${docInfo?.get(1)} ")
    headerRun.addBreak()
    headerRun.setText("Revision number: ${docInfo?.get(2)}")
    headerRun.addBreak()
    headerRun.setText("Revision Date: ${docInfo?.get(3)}")
    headerRun.addBreak()
    headerRun.setText("Title: ${docInfo?.get(0)}")
    footerRun.setText("Prepared By                                                        Approved By ")
    val file = File(folder, fileName)

    // Save the document
    FileOutputStream(file).use { out ->
        doc.write(out)
    }
}

// function to get user input through joption pane
fun getUserInput(prompt: String): String? {
    return JOptionPane.showInputDialog(null, prompt)
}

fun getUserInputs(prompt1: String, prompt2: String, prompt3: String, prompt4: String): List<String?>? {
    val field1 = JTextField(20)
    val field2 = JTextField(20)
    val field3 = JTextField(20)
    val field4 = JTextField(20)
    val panel = JPanel().apply {
        layout = GridLayout(0, 1)
        add(JLabel(prompt1))
        add(field1)
        add(JLabel(prompt2))
        add(field2)
        add(JLabel(prompt3))
        add(field3)
        add(JLabel(prompt4))
        add(field4)
    }
    val result = JOptionPane.showConfirmDialog(
        null,
        panel,
        "Enter Document Data",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    )
    return if (result == JOptionPane.OK_OPTION) {
        listOf(field1.text, field2.text, field3.text, field4.text)
    } else {
        null
    }
}

fun openFilePicker(): String? {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Select logo Image"
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
    val result = fileChooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.absolutePath
    } else {
        null
    }
}

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

fun searchFile(name: String, folder: File): File? {
    val list = folder.listFiles() ?: return null
    for (file in list) {
        var filename = file.name
        filename = if (filename.indexOf(".") > 0) filename.substring(0, filename.lastIndexOf(".")) else filename

        if (file.isDirectory) {
            val result = searchFile(name, file)
            if (result != null) {
                return result
            }
        } else if (name.equals(filename, ignoreCase = true)) {
            return file
        }
    }
    return null
}

fun searchFolder(name: String, folder: File): File? {
    val list = folder.listFiles() ?: return null
    for (file in list) {
        if (file.isDirectory) {
            // Check if the folder name matches
            if (name.equals(file.name, ignoreCase = true)) {
                return file
            }

            // Recursively search within the directory
            val result = searchFolder(name, file)
            if (result != null) {
                return result
            }
        }
    }
    return null
}
//fun displayLogInUI(log: Iterable<RevCommit>) {
//    // Create a JFrame
//    val frame = JFrame("Revision history")
//    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//    frame.setSize(600, 400)
//    // Create a JTextArea
//    val textArea = JTextArea()
//    textArea.isEditable = false
//    // Append log messages to the JTextArea
//    for (commit in log) {
//        textArea.append(" ${commit.fullMessage}\n")
//        textArea.append("------------------------------------------------------------------------------------------\n")
//
//    }
//    if (!log.iterator().hasNext()) {
//        textArea.append("")
//    }
//    // Add the JTextArea to a JScrollPane
//    val scrollPane = JScrollPane(textArea)
//    frame.add(scrollPane, BorderLayout.CENTER)
//
//    // Make the frame visible
//    frame.isVisible = true
//}






