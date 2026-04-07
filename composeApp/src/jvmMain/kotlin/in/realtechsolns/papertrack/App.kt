package `in`.realtechsolns.papertrack

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable



@Composable
fun App() {
    MaterialTheme {
        Column {
            FileTreeItem(folder.value, true)
            showRevisionHistory(filename = currentFileName.value)
            showPreviousVersions(filename = currentFileName.value)
        }
    }
}