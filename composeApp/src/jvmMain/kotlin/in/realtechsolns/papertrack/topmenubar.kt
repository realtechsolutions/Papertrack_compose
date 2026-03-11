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
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import `in`.realtechsolns.papertrack.data.CompanyInfo
import `in`.realtechsolns.papertrack.data.DocumentsFolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FrameWindowScope.AppMenuBar(

    onFolderOpen: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showCompanyDataInput by remember { mutableStateOf(false) }
    val inputs =remember { mutableStateListOf("","","") }
    val labels = remember { mutableStateListOf<String>("Name", "Address", "Phone Number") }
    MenuBar {
        Menu(" Documents    ", mnemonic = 'F') {
            Item(" Add your documents folder and  refresh ", onClick = {
                val userFolder =openFolderPicker()
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
              folder.value =File(userHome, "Papertracks/Docs/Docs")

            })
        }

        Menu(text = "Add company info."){
            Item(" Add company logo ", onClick = {})
            Separator()
            Item(" Add company data ", onClick = {
showCompanyDataInput = !showCompanyDataInput
            })
            Separator()
            Item(" Add company address ", onClick = {})
        }



        Menu(" Org Chart    ") {
            Item("View", onClick = { desktop?.open(orgChart) })
            Item("Edit ", onClick = {desktop?.open(editOrgChart) })
        }

        Menu(" Search   ") {
            Item("Add your documents for search", onClick = onRefresh)
            Item("Search by document no.", onClick = onRefresh)
            Item("Search by revision no.", onClick = onRefresh)
            Item("Search by document title", onClick = onRefresh)
            Item("Search by revision date.", onClick = onRefresh)
            Item("Search by text", onClick = onRefresh)
        }

        Menu(" Help     ") {
            Item("Help", onClick = onRefresh)
            Item("Help2", onClick = onRefresh)
        }

        Menu("Ask questions     ") {
            Item("Ask ", onClick = onRefresh)

        }

    }

    if (showCompanyDataInput) {
        Column {
            inputs.forEachIndexed { index,value ->
                OutlinedTextField(value =value , onValueChange = {
                    inputs[index] = it
                },
                    label = {Text(labels[index])},
                )

            }
            Button(onClick = {

                scope.launch { val companyData = CompanyInfo(name = inputs[0], address = inputs[1],contactNo = inputs[2])
                    companyDao.insert(companyData)
                    // inputs.fill("")
                    showCompanyDataInput = false
                }


            }){ Text("Save data")}
        }


    }


}