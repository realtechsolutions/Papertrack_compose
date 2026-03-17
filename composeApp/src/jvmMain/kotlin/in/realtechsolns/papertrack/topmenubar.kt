package `in`.realtechsolns.papertrack

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import `in`.realtechsolns.papertrack.data.CompanyInfo
import `in`.realtechsolns.papertrack.data.DocumentsFolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.lucene.document.Document
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.DefaultListModel
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JScrollPane

@Composable
fun FrameWindowScope.AppMenuBar(

    onFolderOpen: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showCompanyDataInput by remember { mutableStateOf(false) }
    var showSearchResult by remember { mutableStateOf(false) }
    val inputs = remember { mutableStateListOf("", "", "") }
    val labels = remember { mutableStateListOf<String>("Name", "Address", "Phone Number") }
    MenuBar {
        Menu(" Documents    ", mnemonic = 'F') {
            Item(" Add your documents folder and  refresh ", onClick = {
                val userFolder = openFolderPicker()
                // println("Opening $userFolder")
                scope.launch {
                    userFolder?.let { documentsFolderDao.save(DocumentsFolder(userFolder = it)) }
                    val listfoldertoShow = documentsFolderDao
                        .getAll().first()

                    val userfolderpath = listfoldertoShow[0].userFolder
                    val userFolder = File(userfolderpath)
                    folder.value = userFolder

                }
            })
            Separator()
            Item("Use default sample documents", onClick = {
                folder.value = File(userHome, "Papertracks/Docs/Docs")

            })
        }

        Menu(text = "Add company info.") {
            Item(" Add company logo ", onClick = {})
            Separator()
            Item(" Add company data ", onClick = {
                showCompanyDataInput = !showCompanyDataInput
            })

        }



        Menu(" Org Chart    ") {
            Item("View", onClick = { desktop?.open(orgChart) })
            Item("Edit ", onClick = { desktop?.open(editOrgChart) })
        }

        Menu(" Search   ") {
            Item("Add your documents for search", onClick = {
                LuceneManager.reinitialize()
            })
            Item("Search by document no.", onClick = {
                val query: Query = LuceneManager.docNoParser.parse(JOptionPane.showInputDialog("Enter document no.:"))
                val results = LuceneManager.searcher.search(query, 10)

                for (scoreDoc in results.scoreDocs) {

                //val doc : Document= LuceneManager.searcher.get
                    val doc = LuceneManager.searcher.storedFields().document(scoreDoc.doc)
                    var fileName = doc["fileName"]
                    println(doc)
                    println(fileName)
                }


            })
            Item("Search by revision no.", onClick = {})
            Item("Search by document title", onClick ={})
            Item("Search by revision date.", onClick = {})
            Item("Search by text", onClick = {})
        }

        Menu(" Help     ") {
            Item("""<html>Note:Documents must have header with Revision Number: and Revision Date <br>
              After adding your document folder,add documents to search by clicking add updated documents to search
                  </html>  """, onClick = { })
            Item("Help2", onClick = onRefresh)
        }

        Menu("Ask questions ") {
            Item("Add updated document to search", onClick = {LuceneManager.reinitialize()})
            Item("Search for document number", onClick = {
                val query: Query = LuceneManager.docNoParser.parse(JOptionPane.showInputDialog("Enter document no.:"))
                val results = LuceneManager.searcher.search(query, 10)
                showSearchResults(results)

            })
            Item("search for file name", onClick = onRefresh)
            Item("Search for Revision number", onClick = onRefresh)
            Item("Search for revision date ", onClick = onRefresh)
            Item("Search for text", onClick = {

                val userInput = JOptionPane.showInputDialog("Enter your search query:")
                val query = LuceneManager.textParser.parse(userInput)
                val results = LuceneManager.searcher.search(query, 10)
                showSearchResults(results)
            })

        }

    }

    if (showCompanyDataInput) {
        DialogWindow(onCloseRequest = { showCompanyDataInput = false }, title = "Add company info.") {

            Column {
                inputs.forEachIndexed { index, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            inputs[index] = it
                        },
                        label = { Text(labels[index]) },
                    )

                }
                Button(onClick = {

                    scope.launch {
                        val companyData = CompanyInfo(name = inputs[0], address = inputs[1], contactNo = inputs[2])
                        companyDao.insert(companyData)
                        // inputs.fill("")
                        showCompanyDataInput = false
                    }


                }) { Text("Save data") }
            }

        }
    }


//    fun showSearchResults(results: TopDocs) {
//        val frame = JFrame("Search Results")
//        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//        frame.setSize(800, 400) // Create a list model to manage the list data
//        val listModel = DefaultListModel<String>()
//        val list = JList(listModel) // Add results to the list model
//        for (scoreDoc in results.scoreDocs) {
////        val doc = LuceneManager.searcher.doc(scoreDoc.doc)
////        var fileName = doc["fileName"]
////        fileName = fileName.substringBeforeLast('.')
////        listModel.addElement(fileName)
//        } // Add a mouse listener to handle item clicks
//        list.addMouseListener(object : MouseAdapter() {
//            override fun mouseClicked(e: MouseEvent) {
//                val selectedIndex = list.selectedIndex
//                if (selectedIndex != -1) {
//                    val fileName = listModel.getElementAt(selectedIndex)
//                    // Assuming you have a method to open the file by its name
//                    // println(fileName)
//                    openFile(fileName, folder.value)
//                }
//            }
//        }
//        )
//        val scrollPane = JScrollPane(list)
//        frame.add(
//            scrollPane,
//            BorderLayout.CENTER
//        ) // Show the frame
//        frame.isVisible = true
//    }

//    @Composable
//    fun ShowSearchResults(
//        results: TopDocs,
//        folder: String,
//        openFile: (String, String) -> Unit,
//        onClose: () -> Unit
//    ) {
//
//        val fileNames = remember(results) {
//            results.scoreDocs.map { scoreDoc ->
//                val doc = LuceneManager.searcher.doc(scoreDoc.doc)
//                doc["fileName"].substringBeforeLast('.')
//            }
//        }
//
//        DialogWindow(
//            onCloseRequest = onClose,
//            title = "Search Results"
//        ) {
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(12.dp)
//            ) {
//
//                LazyColumn {
//
//                    items(fileNames) { fileName ->
//
//                        Text(
//                            text = fileName,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    openFile(fileName, folder)
//                                }
//                                .padding(8.dp)
//                        )
//
//                        Divider()
//                    }
//                }
//            }
//        }
//    }


}