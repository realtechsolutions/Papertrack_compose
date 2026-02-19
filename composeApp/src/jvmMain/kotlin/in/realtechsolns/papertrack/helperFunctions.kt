package `in`.realtechsolns.papertrack


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
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.tree.DefaultMutableTreeNode


val rootnode = DefaultMutableTreeNode(folder.name ?: "")
val desktop: Desktop? = Desktop.getDesktop()

fun createTree(folder: File, root: DefaultMutableTreeNode = rootnode): DefaultMutableTreeNode {
    folder.listFiles()?.forEach {
        //if (it.name == ".git") return@forEach
        val subnode = DefaultMutableTreeNode(it.name.substringBeforeLast('.'))
        root.add(subnode)
        if (folder.isDirectory) {
            createTree(it, subnode)
        }
    }
    return root
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






