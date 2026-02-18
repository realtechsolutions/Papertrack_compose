package `in`.realtechsolns.papertrack

import papertrack.composeapp.generated.resources.Res
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URISyntaxException
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
val userHome: String? = System.getProperty("user.home")

var folder: File = File(userHome,"Papertracks/Docs/Docs")
//var folder:File = File("app/resources)//Docs")
//var folder:File = File("app/resources/orgChart/index.html")

class DocsPanel(frame:JFrame) : JPanel() {
    init {
        layout = BorderLayout()
        try {
            //val doc = ClassLoader.getSystemResource("Docs")//



           // println (" first debug $doc")
            //folder = File(prefs.get("userDocsFolder", "app/resources//Docs"))
            //folder = File(doc.toURI())
           // folder = File(doc.path)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        val treeModel = DefaultTreeModel(createTree(folder))
        val tv = JTree(treeModel)
        tv.font = Font("SansSerif", Font.PLAIN, 16)
        tv.isRootVisible = false
        tv.showsRootHandles = true
//        val treepopup = TreePopup(tv)
//        val treepopupFile = TreePopupFile(tv,frame)
        tv.isOpaque = true
        // Add mouse listener for popup and file opening
        tv.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val node = tv.lastSelectedPathComponent as? DefaultMutableTreeNode
                node?.let {
                    openFile(it.toString(), folder)
                }
            }
//            override fun mouseReleased(e: MouseEvent?) {
//                super.mouseReleased(e)
//                if (e != null) {
//                    val path = tv.getPathForLocation(e.x, e.y)
//                    if (path != null) {
//                        tv.selectionPath = path
//                    }
//                    val node = tv.lastSelectedPathComponent as? DefaultMutableTreeNode
//                    selectedNode = node
//                    if (e.isPopupTrigger() && searchFile(selectedNode.toString(), folder)?.isFile == true)
//                    { treepopupFile.show(e.component, e.x, e.y)
//                    }
//                    else if
//                                 (e.isPopupTrigger() &&  searchFolder(selectedNode.toString(), folder)?.isDirectory == true)
//                    {
//                        treepopup.show(e.component, e.x, e.y)
//                    }
//                }
//            }
        })
        val scrollPane = JScrollPane(tv)
        scrollPane.isOpaque = true
        add(scrollPane, BorderLayout.CENTER)
    }
}