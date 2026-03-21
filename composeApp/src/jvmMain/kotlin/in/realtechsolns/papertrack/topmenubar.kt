package `in`.realtechsolns.papertrack

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import `in`.realtechsolns.papertrack.data.CompanyInfo
import `in`.realtechsolns.papertrack.data.DocumentsFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JOptionPane

@Composable
fun FrameWindowScope.AppMenuBar(
    onClick: () -> Unit,
    onFolderOpen: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {

    val scope = rememberCoroutineScope()
    var showCompanyDataInput by remember { mutableStateOf(false) }
    var showSearchResult by remember { mutableStateOf(false) }
    val inputs = remember { mutableStateListOf("", "", "") }
    val labels = remember { mutableStateListOf<String>("Name", "Address", "Phone Number") }
    var fileSearchResults by remember { mutableStateOf<List<String>>(emptyList()) }

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
                scope.launch {
                    showLoaderSearchFiles.value =!showLoaderSearchFiles.value
                    withContext(Dispatchers.IO){
                        val documentSearchItemsList = getAllDocxContents(folder.value.absolutePath)
                        documentSearchDa0.deleteAll()
                        documentSearchDa0.insertDocumentSearchItems(documentSearchItemsList)
                    }

              // println(" debugging $documentSearchItemsList")
                showLoaderSearchFiles.value = !showLoaderSearchFiles.value
                }
            })
            Item("Search by document no.", onClick = {
              val userQuery = JOptionPane.showInputDialog("Enter document Number to search")
                scope.launch {
               fileSearchResults = documentSearchDa0.getFilePathsFlexible(userQuery) }
               showSearchResult = !showSearchResult
            })
            Item("Search by revision no.", onClick = {
                val userQuery = JOptionPane.showInputDialog("Enter revision Number to search")
                scope.launch {
                    fileSearchResults = documentSearchDa0.getFilePathsFlexible(revNo = userQuery.toInt()) }
                showSearchResult = !showSearchResult

            })
            Item("Search by document title", onClick ={
                val userQuery = JOptionPane.showInputDialog("Enter title to search")
                scope.launch {
                    fileSearchResults = documentSearchDa0.getFilePathsFlexible(title =userQuery) }
                showSearchResult = !showSearchResult
            })

            Item("Search by revision date.", onClick = {
                val userQuery = JOptionPane.showInputDialog("Enter revision date to search")
                scope.launch {
                    fileSearchResults = documentSearchDa0.getFilePathsFlexible(revDate = userQuery) }
                showSearchResult = !showSearchResult

            })

            Item("Search by text", onClick = {

               val userQuery = JOptionPane.showInputDialog("Enter text to search")
              scope.launch {
               //println   (contentSearchDa.searchByText(userQuery))
               //println   (contentSearchDao.getFileNamesByText(userQuery))
                 fileSearchResults =  contentSearchDao.getFileNamesByText(userQuery)
                  showSearchResult = !showSearchResult

              }


            })
        }

        Menu(" Help     ") {
            Item(text = "View Help" , onClick = onClick


            )
//            Item("""<html>Note:Documents must have header with Revision Number: and Revision Date <br>
//              After adding your document folder,add documents to search by clicking add updated documents to search
//                  </html>  """, onClick = { })
//            Item("Help2", onClick = onRefresh)
        }





//        Menu("Ask questions ") {
//            Item("Add updated document to search", onClick = {LuceneManager.reinitialize()})
//            Item("Search for document number", onClick = {
//                val query: Query = LuceneManager.docNoParser.parse(JOptionPane.showInputDialog("Enter document no.:"))
//                val results = LuceneManager.searcher.search(query, 10)
//                showSearchResults(results)
//
//            })
//            Item("search for file name", onClick = onRefresh)
//            Item("Search for Revision number", onClick = onRefresh)
//            Item("Search for revision date ", onClick = onRefresh)
//            Item("Search for text", onClick = {
//
//                val userInput = JOptionPane.showInputDialog("Enter your search query:")
//                val query = LuceneManager.textParser.parse(userInput)
//                val results = LuceneManager.searcher.search(query, 10)
//                showSearchResults(results)
//            })
//
//        }

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
                       companyDao.deleteAll()
                        companyDao.insert(companyData)
                        // inputs.fill("")
                        showCompanyDataInput = false
                    }
                }) { Text("Save data") }
            }

        }
    }


    if(showSearchResult) {
      DialogWindow(onCloseRequest = { showSearchResult= false }, title = "Search results", )   {
FileListTextSearch(fileSearchResults)
      }
    }
}