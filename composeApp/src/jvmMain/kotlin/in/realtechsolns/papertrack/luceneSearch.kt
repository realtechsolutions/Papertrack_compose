package `in`.realtechsolns.papertrack

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JOptionPane


//object LuceneManager {
//    val analyzer = StandardAnalyzer()
//    val indexPath = Paths.get("index")
//    lateinit var index: Directory
//    lateinit var writer: IndexWriter
//    lateinit var searcher: IndexSearcher
//    lateinit var docNoParser: QueryParser
//    lateinit var textParser: QueryParser
//    lateinit var titleParser: QueryParser
//    lateinit var revNoParser: QueryParser
//    lateinit var revDateParser: QueryParser
//    //@Synchronized
//    fun initialize() {
//        index = FSDirectory.open(indexPath)
//        val config = IndexWriterConfig(analyzer)
//        writer = IndexWriter(index, config)
//        if (!DirectoryReader.indexExists(index)) {
//            // println("index does not exist or is empty")
//            indexFilesInDirectory(writer, folder)
//            //println("files indexed")
//        }
//        writer.commit()
//        val reader = DirectoryReader.open(index)
//        textParser = QueryParser("content", analyzer)
//        docNoParser = QueryParser("docNo", analyzer)
//        titleParser = QueryParser("title", analyzer)
//        revNoParser = QueryParser("revNo", analyzer)
//        revDateParser = QueryParser("revDate", analyzer)
//        searcher = IndexSearcher(reader)
//        writer.close()
//    }
//    // @Synchronized
//    fun reinitialize() { // Get the path from the index directory
//        println(!DirectoryReader.indexExists(index))
//        //val indexPath = indexPath.toAbsolutePath() // Delete existing index directory content
//        if (DirectoryReader.indexExists(index)) {
//            Files.walk(indexPath).sorted(Comparator.reverseOrder()).forEach(Files::delete)
//            println("Index directory content deleted")
//        } // Reinitialize the index
//        initialize()
//        JOptionPane.showMessageDialog(
//            null,
//            "Search updated with latest documents .",
//            "Search update",
//            //JOptionPane.WARNING_MESSAGE
//            JOptionPane.INFORMATION_MESSAGE
//        )
//    }
//}
//// Function to read Word document and add its content to Lucene index
//fun addDoc(writer: IndexWriter, file: File) {
//    val doc = XWPFDocument(FileInputStream(file))
//    val paragraphs = doc.paragraphs
//    val contentBuilder = StringBuilder()
//    for (para in paragraphs) {
//        contentBuilder.append(para.text).append("\n")
//    }
//    val headerPolicy = doc.headerFooterPolicy
//    val header = headerPolicy.defaultHeader ?: return
//    val fullHeaderText = StringBuilder()
//    for (paragraph in header.paragraphs) {
//        for (run in paragraph.runs) {
//            fullHeaderText.append(run.text())
//        }
//    }
//    val headerText = fullHeaderText.toString()
//    val documentNoRegex = Regex("Document No:(.*)")
//    val titleRegex = Regex("Title:(.*)")
//    val revNoRegex = Regex("Revision Number:(.*)")
//    val revDateRegex = Regex("Revision Date:(.*)")
//    val docNo = documentNoRegex.find(headerText)?.groups?.get(1)?.value?.trim() ?: "No Document No found"
//    val title = titleRegex.find(headerText)?.groups?.get(1)?.value?.trim() ?: "No Document No found"
//    val revNo = revNoRegex.find(headerText)?.groups?.get(1)?.value?.trim() ?: "No Revision Number found"
//    val revDate = revDateRegex.find(headerText)?.groups?.get(1)?.value?.trim() ?: "No Revision Date found"
//    val fileName = file.name
//    val content = contentBuilder.toString()
//    val luceneDoc = Document()
//    luceneDoc.add(TextField("title", title, Field.Store.YES))
//    luceneDoc.add(StringField("docNo", docNo, Field.Store.YES))
//    luceneDoc.add(StringField("revNo", revNo, Field.Store.YES))
//    luceneDoc.add(TextField("content", content, Field.Store.YES))
//    luceneDoc.add(StringField("fileName", fileName, Field.Store.YES))
//    luceneDoc.add(StringField("revDate", revDate, Field.Store.YES))
//    writer.addDocument(luceneDoc)
//}
//
//// Function to traverse directories and add Word documents to the index
//fun indexFilesInDirectory(writer: IndexWriter, folder: File) {
//    val files = folder.listFiles() ?: return
//    for (file in files) {
//        if (file.isDirectory) {
//            indexFilesInDirectory(writer, file)
//        } else if (file.extension.equals("docx", ignoreCase = true)) {
//            try {
//                RandomAccessFile(file, "rw").close()
//            } catch (e: IOException) {
//                JOptionPane.showMessageDialog(
//                    null,
//                    "Please close the file before updating search .",
//                    "File Open",
//                    JOptionPane.WARNING_MESSAGE
//                )
//                return
//            }
//            addDoc(writer, file)
//        }
//    }
//
//}
//
//
//
