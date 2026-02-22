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
import org.apache.poi.hpsf.Date
import org.apache.poi.util.Units
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFFooter
import org.apache.poi.xwpf.usermodel.XWPFHeader
import java.awt.Desktop
import java.awt.GridLayout
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

private fun updateFile(file: File) {
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

    val revDateRegex = Regex("Revision Date: \\d{2}/\\d{2}/\\d{4}")
    val revNumberRegex = Regex("Revision [Nn]umber: (\\d+)")
    var newRevNumber = 1

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
            newRevNumber = currentRev + 1

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
    println("$newRevNumber updated")
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






