package `in`.realtechsolns.papertrack


import java.awt.Desktop
import java.io.File
import java.io.IOException
import javax.swing.tree.DefaultMutableTreeNode


val rootnode = DefaultMutableTreeNode(folder?.name ?: "")
val desktop = Desktop.getDesktop()

fun createTree(folder: File, root: DefaultMutableTreeNode = rootnode): DefaultMutableTreeNode {
    folder.listFiles()?.forEach {
        if (it.name == ".git") return@forEach
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
            desktop.open(file)
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
                desktop.open(file)
            } catch (e: IOException) {
            }
        } else {
            if (file.isDirectory) {
                openFolder(name, file)
            }
        }
    }
}

