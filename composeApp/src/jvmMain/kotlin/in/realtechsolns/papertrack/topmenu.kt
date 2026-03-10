package `in`.realtechsolns.papertrack


import org.apache.lucene.index.Term
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.util.prefs.Preferences
import javax.swing.*

//class TopMenu(private val prefs: Preferences, private val frame: JFrame) : JMenuBar() {
//    init {
//        font = Font("SansSerif", Font.PLAIN, 16)
//        val docs = JMenu("Docs")
//        docs.add(createMenuItem(" Add your documents folder and start again to refresh ")
//        {
//            prefs.put("userDocsFolder", openFolderPicker())
//        }
//        )
//        docs.font = Font("SansSerif", Font.PLAIN, 16)
//        add(Box.createHorizontalStrut(20))
//        add(docs)
//        add(Box.createHorizontalStrut(100))
//        // Add "Add company info." menu
//        val addCompanyInfoMenu = JMenu("Add company info.")
//        addCompanyInfoMenu.font = Font("SansSerif", Font.PLAIN, 16)
//        addCompanyInfoMenu.add(createMenuItem("Company logo") {
//            prefs.put("logoImage", openFilePicker())
//        })
//        addCompanyInfoMenu.add(createMenuItem("Company name") {
//            prefs.put("companyName", getUserInput("Enter company name"))
//        })
//        addCompanyInfoMenu.add(createMenuItem("Company address") {
//            prefs.put("companyAddress", getUserInput("Enter company address"))
//        })
//        add(addCompanyInfoMenu)
//        add(Box.createHorizontalStrut(120))
//        val orgChart = JMenu("Organisation Chart")
//        orgChart.font = Font("SansSerif", Font.PLAIN, 16)
//        orgChart.add(createMenuItem("View") {
//            desktop?.open(File("app/resources/orgChart/index.html"))
//        })
//        orgChart.add(createMenuItem("Edit") {
//            desktop?.open(File("app/resources/orgChart/edit.html"))
//        })
//        add(orgChart)
//        add(Box.createHorizontalStrut(120))
//        // Add "Search" menu
//        val helpMenu = JMenu("Help")
//        helpMenu.font = Font("SansSerif", Font.PLAIN, 16)
//
//        helpMenu.add(
//            createMenuItem("""<html>Note:Documents must have header with Revision Number: and Revision Date <br>
//              After adding your document folder,add documents to search by clicking add updated documents to search
//                  </html>  """){})
////        helpMenu.font = Font("SansSerif", Font.PLAIN, 16)
////        val popupMenu = JPopupMenu()
////        val label = JLabel("<html>Note:Documents must have header with Revision Number: and Revision Date<br>" +
////                "After adding your document folder  ,add documents to search by clicking add updated documents to search " +
////                   "</html>")
////        popupMenu.add(label)
////
////        helpMenu.addMouseListener(object : MouseAdapter() {
////         override fun mouseClicked(e: MouseEvent?) {
////             popupMenu.show(helpMenu, e!!.x, e.y + helpMenu.height) } })
//        add(helpMenu)
//        add(Box.createHorizontalStrut(120))
//        val searchMenu = JMenu("Search")
//        searchMenu.font = Font("SansSerif", Font.PLAIN, 16)
//        searchMenu.add(createMenuItem("Add updated documents to search") {
//            //LuceneManager.reinitialize()
//        })
//        searchMenu.add(createMenuItem("Search for document No.") {
//            val query: Query = LuceneManager.docNoParser.parse(JOptionPane.showInputDialog("Enter document no.:"))
//            val results = LuceneManager.searcher.search(query, 10)
//            showSearchResults(results)
//        })
//        searchMenu.add(createMenuItem("Search for revision  No.") {
//            val query: Query = LuceneManager.revNoParser.parse(JOptionPane.showInputDialog("Enter revision no.:"))
//            val results = LuceneManager.searcher.search(query, 10)
//            showSearchResults(results)
//        })
//        searchMenu.add(createMenuItem("Search for revision date") {
//            val revDate = JOptionPane.showInputDialog("Enter the revision date to search for (format: DD-MM-YYYY):")
//            val query = TermQuery(Term("revDate", revDate))
//            val results = LuceneManager.searcher.search(query, 10)
//            showSearchResults(results)
//        })
//        searchMenu.add(createMenuItem("Search for document title.") {
//            val query: Query = LuceneManager.titleParser.parse(JOptionPane.showInputDialog("Enter document title:"))
//            val results = LuceneManager.searcher.search(query, 10)
//            showSearchResults(results)
//        })
//        searchMenu.add(createMenuItem("Search for text.")
//        {
//            val userInput = JOptionPane.showInputDialog("Enter your search query:")
//            val query = LuceneManager.textParser.parse(userInput)
//            val results = LuceneManager.searcher.search(query, 10)
//            showSearchResults(results)
//        }
//        )
//        add(searchMenu)
//        add(Box.createHorizontalStrut(120))
//        val askQuestionMenu = JMenu("Ask question")
//        askQuestionMenu.font = Font("SansSerif", Font.PLAIN, 16)
//        askQuestionMenu.add(createMenuItem("Ask Question") {
//            try {
//                val userInput = JOptionPane.showInputDialog("Enter your question:")
//                val query = LuceneManager.textParser.parse(userInput)
//                val results = LuceneManager.searcher.search(query, 10)
//                val hits = results.scoreDocs
//                val docId = hits[0].doc
//                //val doc = LuceneManager.searcher.doc(docId)
//               // val content = doc.get("content")
////                println(content)
////                getResponse(userInput,content)
//                // Generate text using the HuggingFaceService
////           val generatedText = huggingFaceService.generateText(prompt, parameters = parameters)
////           println("Generated text: $generatedText")
//            } catch (e: IOException) {
//                println("Error: ${e.message}")
//                println("Error: reeor77")
//
//
//            }
//
//
//
////       val stringBuilder = StringBuilder()
////       for(hit in hits ){
////           if (hit.score>0.99) {
////               val docId = hit.doc
////               val doc = LuceneManager.searcher.doc(docId)
////               val content = doc.get("content")
////              stringBuilder.append(content)
////           }
////       }
//
//            //println(context)
//            //getResponse()
//        })
//        add(askQuestionMenu)
//    }
//
//
//}
//
//
//private fun showSearchResults(results: TopDocs) {
//    val frame = JFrame("Search Results")
//    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//    frame.setSize(800, 400) // Create a list model to manage the list data
//    val listModel = DefaultListModel<String>()
//    val list = JList(listModel) // Add results to the list model
//    for (scoreDoc in results.scoreDocs) {
////        val doc = LuceneManager.searcher.doc(scoreDoc.doc)
////        var fileName = doc["fileName"]
////        fileName = fileName.substringBeforeLast('.')
////        listModel.addElement(fileName)
//    } // Add a mouse listener to handle item clicks
//    list.addMouseListener(object : MouseAdapter() {
//        override fun mouseClicked(e: MouseEvent) {
//            val selectedIndex = list.selectedIndex
//            if (selectedIndex != -1) {
//                val fileName = listModel.getElementAt(selectedIndex)
//                // Assuming you have a method to open the file by its name
//                // println(fileName)
//                openFile(fileName, folder)
//            }
//        }
//    }
//    )
//    val scrollPane = JScrollPane(list)
//    frame.add(
//        scrollPane,
//        BorderLayout.CENTER
//    ) // Show the frame
//    frame.isVisible = true
//}
//
//private fun createMenuItem(title: String, action: () -> Unit): JMenuItem {
//    return JMenuItem(title).apply {
//        addActionListener { action() }
//    }
//}
//
//
//fun showAnswer(){
//    val frame = JFrame("Search Results")
//    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//    frame.setSize(800, 400)
//
//
//}
//
//
//
