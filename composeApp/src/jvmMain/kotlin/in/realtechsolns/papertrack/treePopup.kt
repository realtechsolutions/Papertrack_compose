package `in`.realtechsolns.papertrack

//import composables.*
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
//import org.eclipse.jgit.revwalk.RevCommit
//import org.eclipse.jgit.treewalk.TreeWalk
//import org.eclipse.jgit.treewalk.filter.PathFilter
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.tree.DefaultMutableTreeNode

// select folder from treeview
// selected node is set in mouse release function in Docs composable in mainScreen.kt
var selectedNode: DefaultMutableTreeNode? = null
var selectedFile: File? = null
// class for creating treepopup object
class TreePopup(tree: JTree?) : JPopupMenu() {
    init {
        val delete = JMenuItem("Delete folder")
        val add = JMenuItem("Add file")
        delete.addActionListener {
            folder?.let { it1 -> delete(selectedNode.toString(), it1) }
            JOptionPane.showMessageDialog(
                null,
                "File deleted successfully!",
                "File Deletion",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
        add.addActionListener {
            getUserInput("Enter file name")?.let { it1 ->
                //getUserInputs("p1","p2","p3")
                addFile(
                    selectedNode.toString(),
                    "$it1.docx"
                )
            }
        }
        add(delete)
        add(JSeparator())
        add(add)
        add(JSeparator())
    }
}

class TreePopupFile(val tree: JTree?, frame: JFrame) : JPopupMenu() {
    init {
        val update = JMenuItem("Update")
        val delete = JMenuItem("Delete file ")
        val reName = JMenuItem("Rename file")
        val viewHistory = JMenuItem("View revision history")
        val viewPrevious = JMenuItem("View previous versions")
        delete.addActionListener {
            folder?.let { it1 -> delete(selectedNode.toString(), it1) }
            //showToast(frame,"file deleted|n Refresh Screen",1500)
            JOptionPane.showMessageDialog(
                null,
                "File deleted successfully!",
                "File Deletion",
                JOptionPane.INFORMATION_MESSAGE
            )
        }

        update.addActionListener {
           // updateFile()
            JOptionPane.showMessageDialog(
                null,
                "File updated successfully",
                "File updated",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
        viewHistory.addActionListener { showRevHistory() }
        viewPrevious.addActionListener { viewPreviousVersion() }
        add(update)
        add(delete)
        add(JSeparator())
        add(JSeparator())
        add(viewHistory)
        add(viewPrevious)
    }

    override fun show(invoker: Component, x: Int, y: Int) {
        // Get screen dimensions
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val popupSize = preferredSize
        val invokerLocation = invoker.locationOnScreen
        // Calculate available space
        val availableSpaceBelow = screenSize.height - (invokerLocation.y + y)
        val availableSpaceAbove = invokerLocation.y + y
        // Add some padding to ensure menu isn't right at screen edge
        val PADDING = 10
        // Calculate adjusted Y position
        val adjustedY = when {
            // If enough space below, show normally
            availableSpaceBelow >= popupSize.height + PADDING -> y
            // If enough space above, show above the node
            availableSpaceAbove >= popupSize.height + PADDING -> y - popupSize.height
            // If neither above nor below has enough space, show where more space is available
            availableSpaceAbove > availableSpaceBelow -> 0
            else -> y - (popupSize.height - availableSpaceBelow)
        }
        // Calculate adjusted X position to prevent horizontal overflow
        val availableSpaceRight = screenSize.width - (invokerLocation.x + x)
        val adjustedX = if (availableSpaceRight < popupSize.width) {
            x - popupSize.width
        } else {
            x
        }
        super.show(invoker, adjustedX, adjustedY)
        tree?.requestFocusInWindow()
    }

//    private fun updateFile() {
//        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//        val file = folder?.let { searchFile(selectedNode.toString(), it) } ?: return
//        selectedFile = file
//        var newRevNumber = 1
//        try {
//            RandomAccessFile(file, "rw").close()
//        } catch (e: IOException) {
//            JOptionPane.showMessageDialog(
//                null,
//                "Please close the file before updating.",
//                "File Open",
//                JOptionPane.WARNING_MESSAGE
//            )
//            return
//        }
//        FileInputStream(file).use { inStream ->
//            val doc = XWPFDocument(inStream)
//            val headerPolicy = doc.headerFooterPolicy
//            val header = headerPolicy.defaultHeader ?: return
//
//            // First, find the revision number and build complete text
//            val fullHeaderText = StringBuilder()
//            for (paragraph in header.paragraphs) {
//                for (run in paragraph.runs) {
//                    fullHeaderText.append(run.text())
//                }
//            }
//            // Find and update the revision number first
//            val revNumberRegex = Regex("Revision [Nn]umber: (\\d+)")
//            val revNumberMatch = revNumberRegex.find(fullHeaderText.toString())
//            val currentRevNumber = revNumberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
//            newRevNumber = currentRevNumber + 1
//            // Now process each paragraph carefully
//            for (paragraph in header.paragraphs) {
//                val runs = paragraph.runs
//                val paragraphText = StringBuilder()
//                for (run in runs) {
//                    paragraphText.append(run.text())
//                }
//                var updatedText = paragraphText.toString()
//                // Only update if this paragraph contains revision information
//                if (updatedText.contains("Revision", ignoreCase = true)) {
//                    val revDateRegex = Regex("Revision Date: \\d{2}/\\d{2}/\\d{4}")
//
//                    // Keep track of original text length
//                    val originalLength = updatedText.length
//                    // Update the revision date
//                    updatedText = revDateRegex.replace(updatedText, "Revision Date: $currentDate")
//                    // Update the revision number, ensuring we don't affect other parts
//                    updatedText =
//                        revNumberRegex.replace(updatedText, "Revision Number: ${String.format("%02d", newRevNumber)}")
//                    // Ensure we're not truncating
//                    if (runs.size == 1) {
//                        // If there's only one run, simply update it
//                        runs[0].setText(updatedText, 0)
//                    } else {
//                        // For multiple runs, carefully preserve the structure
//                        var currentIndex = 0
//                        for (run in runs) {
//                            val runLength = run.text().length
//                            if (currentIndex + runLength <= updatedText.length) {
//                                run.setText(updatedText.substring(currentIndex, currentIndex + runLength), 0)
//                                currentIndex += runLength
//                            } else if (currentIndex < updatedText.length) {
//                                run.setText(updatedText.substring(currentIndex), 0)
//                                currentIndex = updatedText.length
//                            } else {
//                                run.setText("", 0)
//                            }
//                        }
//                    }
//                }
//            }
//            FileOutputStream(file).use { outStream ->
//                doc.write(outStream)
//            }
//        }
//        val repoRoot = folder?.toPath()?.toAbsolutePath()?.normalize()
//        val filePath = file.toPath().toAbsolutePath().normalize()
//        val relativePath = repoRoot?.relativize(filePath)?.toString()?.replace("\\", "/")
//        relativePath?.let { path ->
////            git.add().addFilepattern(path).call()
////            getUserInput("Enter Reason for revision")?.let { input ->
////                git.commit()
////                    .setMessage(
////                        "Revision Date: $currentDate Revision Number: ${
////                            String.format(
////                                "%02d",
////                                newRevNumber
////                            )
////                        } $input"
////                    )
////                    .call()
////            }
//        }
//
//    }


    private fun showRevHistory() {
        val file = folder?.let { it1 -> searchFile(selectedNode.toString(), it1) }
        selectedFile = file
        if (file != null) {
            try {
                RandomAccessFile(file, "rw").close()
            } catch (e: IOException) {
                // If an IOException is thrown, the file is likely open elsewhere
                JOptionPane.showMessageDialog(
                    null,
                    "Please close the file before updating.",
                    "File Open",
                    JOptionPane.WARNING_MESSAGE
                )
                return
            }
            val repoRoot = folder?.toPath()?.toAbsolutePath()?.normalize()
            val filePath = selectedFile?.toPath()?.toAbsolutePath()?.normalize()
            val relativePath = filePath?.let { it1 -> repoRoot?.relativize(it1).toString().replace("\\", "/") }
//            val log: Iterable<RevCommit> = git.log().addPath(relativePath).call()
//            displayLogInUI(log)
        } else {
            println("No default header found.")
        }
    }
}

fun viewPreviousVersion() {
    val file = folder?.let { it1 -> searchFile(selectedNode.toString(), it1) }
    selectedFile = file
    val repoRoot = folder?.toPath()?.toAbsolutePath()?.normalize()
    val filePath = selectedFile?.toPath()?.toAbsolutePath()?.normalize()
    val relativePath = filePath?.let { it1 -> repoRoot?.relativize(it1).toString().replace("\\", "/") }
   // val log: Iterable<RevCommit> = git.log().addPath(relativePath).call()
    //val list = log.toList()
    val columnNames = arrayOf("Hash", "Date")
    val tableModel = DefaultTableModel(columnNames, 0) // Populate the table model
//    for (commit in list) {
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
//        val formattedDate = dateFormat.format(Date(commit.commitTime.toLong() * 1000))
//        val rowData = arrayOf(commit.id.name, formattedDate)
//        tableModel.addRow(rowData)
//    }
    val table = JTable(tableModel)
    val scrollPane = JScrollPane(table)
    table.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            println("dobleclick detected")
            val row = table.selectedRow
            if (row != -1) {
                val searchHash = (tableModel.getValueAt(row, 0) as String).trim()
                if (relativePath != null) {
                    //openDocument(log,commitHash,relativePath)
//                    val targetCommit: RevCommit? = list.find { commit -> commit.name == searchHash }
//                    val treeWalk = TreeWalk(git.repository)
//                    if (targetCommit != null) {
//                        treeWalk.addTree(targetCommit.tree)
//                        treeWalk.isRecursive = true
//                        treeWalk.setFilter(PathFilter.create(relativePath))
//                        var fileContent: ByteArray? = null
//                        while (treeWalk.next()) {
//                            if (treeWalk.pathString == relativePath) {
//                                val objectId = treeWalk.getObjectId(0)
//                                val loader = git.repository.open(objectId)
//                                fileContent = loader.bytes
//                                break
//                            }
                        }
//                        if (fileContent == null) {
//                            println("File not found")
//                            return
//                        }
//                        val tempFile = File.createTempFile("doc", ".docx")
//                        FileOutputStream(tempFile).use { outputStream ->
//                            outputStream.write(fileContent)
//                        }
                        //esktop.open(tempFile)
                    }
                }
            //}
       // }
    })
    val dialog = JDialog().apply {
        title = "Document version Viewer"
        contentPane.add(scrollPane)
        setSize(800, 600)
        setLocationRelativeTo(null)
        isModal = true
        defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
    }
    dialog.isVisible = true
}


























