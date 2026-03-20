package `in`.realtechsolns.papertrack

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf


var showLoaderSearchFiles = mutableStateOf(false)
@Composable
//@Preview
fun App() {
    MaterialTheme {
        Column {
            FileTreeItem(folder.value, true)
            showRevisionHistory(filename = currentFileName.value)
            showPreviousVersions(filename = currentFileName.value)
            if (showLoaderSearchFiles.value) {
                showLoader("...Loading files for search")
            }
        }
    }
}