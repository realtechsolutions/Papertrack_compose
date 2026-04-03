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
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JOptionPane
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

@Composable
fun FrameWindowScope.AppMenuBar(
    onClick: () -> Unit,
    onFolderOpen: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit,
    showLoader : MutableState<Boolean>
) {

    val scope = rememberCoroutineScope()
    var showCompanyDataInput by remember { mutableStateOf(false) }
    var showSearchResult by remember { mutableStateOf(false) }
    val inputs = remember { mutableStateListOf("", "", "") }
    val labels = remember { mutableStateListOf<String>("Name", "Address", "Phone Number") }
    var fileSearchResults by remember { mutableStateOf<List<String>>(emptyList()) }

    MenuBar {
        Menu(" Documents    ", mnemonic = 'F') {
            Item(" Add your documents folder ", onClick = {
                val userFolder = openFolderPicker()
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


        Menu(text = "Master list documents    "){

           Item(text = "Create/Update Master list", onClick = {
               val masterFilePath = Path.of(userHome,"masterlist.xlsx").toAbsolutePath().toString()
               println(masterFilePath)

               val pathString = folder.value.absolutePath
              val path = Path.of(pathString)

               XSSFWorkbook().use { workbook ->
                   val sheet = workbook.createSheet("Master list")
                   val headerRow = sheet.createRow(0)
                   headerRow.createCell(0).setCellValue("SN")
                   headerRow.createCell(1).setCellValue("File name")

                   var rowIndex = 1
                   var sn =1
                   Files.walk(path).use {stream ->
                    stream.forEach {
                       val row = sheet.createRow(rowIndex++)
                        val firstCell = row.createCell(0)
                        if (it.isDirectory()){firstCell.setCellValue("")}
                        if (it.isRegularFile()){firstCell.setCellValue(sn.toString())}
                        //row.createCell(0).setCellValue(sn.toString())
                        row.createCell(1).setCellValue(it.fileName.toString().removeSuffix(".docx"))
                        if (it.isRegularFile()){ sn++}



                    }

                       FileOutputStream(masterFilePath).use {outputStream ->
                         workbook.write(outputStream)

                       }
                       println("$masterFilePath exported")



                   }





               }



//               Files.walk(path).use{stream ->
//
//
//                   stream.forEach {
//
//
//                     println(it.fileName.toString()) }
//
//               }

           })
           Item(text = "View Master list", onClick = {})

        }

        Menu(" Search   ") {
            Item("Add your documents for search", onClick = {
                scope.launch {
                   // showLoaderSearchFiles.value =!showLoaderSearchFiles.value
                    showLoader.value = true
                    withContext(Dispatchers.IO){
                        val documentSearchItemsList = getAllDocxContents(folder.value.absolutePath)
                        documentSearchDa0.deleteAll()
                        documentSearchDa0.insertDocumentSearchItems(documentSearchItemsList)
                    }

              // println(" debugging $documentSearchItemsList")
               // showLoaderSearchFiles.value = !showLoaderSearchFiles.value
                    showLoader.value = false
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