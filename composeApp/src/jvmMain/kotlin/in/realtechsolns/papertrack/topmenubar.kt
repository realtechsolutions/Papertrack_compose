package `in`.realtechsolns.papertrack

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

@Composable
fun FrameWindowScope.AppMenuBar(
    onFolderOpen: () -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    MenuBar {
        Menu(" Documents    ", mnemonic = 'F') {
//            Item("Open Folder", onClick = onFolderOpen)
//            Separator()
//            Item("Exit", onClick = onExit)
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
}