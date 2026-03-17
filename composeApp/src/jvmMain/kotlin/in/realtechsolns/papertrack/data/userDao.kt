package `in`.realtechsolns.papertrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

//@Dao
//interface UserDao {
//
//    @Insert (onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(user: User)
//
//    @Query("SELECT * FROM User")
//    suspend fun getAll(): List<User>
//}

@Dao
interface CompanyDao  {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: CompanyInfo)

    @Query("SELECT * FROM CompanyInfo")
    suspend fun getAll(): List<CompanyInfo>
}

@Dao
interface DocumentRevisionDao {

    // Basic insert (internal use only)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRevisionInternal(documentRevision: DocumentRevision): Long


    // Get next revision number
    @Query("""
        SELECT COALESCE(MAX(revNumber), 0) + 1
        FROM DocumentRevision
        WHERE documentNo = :docNo
    """)
    suspend fun getNextRevisionNumber(docNo: String): Int


    // Delete older revisions (keep latest 5)
    @Query("""
        DELETE FROM DocumentRevision
        WHERE id NOT IN (
            SELECT id FROM DocumentRevision
            WHERE documentNo = :docNo
            ORDER BY revNumber DESC
            LIMIT 5
        )
        AND documentNo = :docNo
    """)
    suspend fun deleteOlderRevisions(docNo: String)


    // 🔥 MAIN FUNCTION: Insert and enforce max 5 revisions
    @Transaction
    suspend fun insertRevisionKeepingLastFive(
        documentNo: String,
        revReason: String,
        title: String,
        fileName : String,
        filePath: String
    ) {
        val nextRev = getNextRevisionNumber(documentNo)

        val revision = DocumentRevision(
            documentNo = documentNo,
            revNumber = nextRev,
            revReason = revReason,
            title = title,
            fileName = fileName,
            filePath = filePath
        )

        insertRevisionInternal(revision)

        // After insert, remove older revisions
        deleteOlderRevisions(documentNo)
    }


    // Get revisions
    @Query("""
        SELECT * FROM DocumentRevision
        WHERE documentNo = :docNo
        ORDER BY revNumber DESC
    """)
    suspend fun getRevisionsOfDocument(docNo: String): List<DocumentRevision>


    // Get latest revision
    @Query("""
        SELECT * FROM DocumentRevision
        WHERE documentNo = :docNo
        ORDER BY revNumber DESC
        LIMIT 1
    """)
    suspend fun getLatestRevision(docNo: String): DocumentRevision?


    @Query("DELETE FROM DocumentRevision WHERE fileName = :fileName AND id NOT IN (SELECT id FROM DocumentRevision WHERE fileName = :fileName ORDER BY revNumber DESC LIMIT 3)")
    suspend fun keepOnlyLatestThree(fileName: String)

    @Query("SELECT filePath FROM DocumentRevision WHERE fileName = :fileName AND id NOT IN (SELECT id FROM DocumentRevision WHERE fileName = :fileName ORDER BY revNumber DESC LIMIT 3)")
    suspend fun getFilePathsToDelete(fileName: String): List<String>

    @Query("""
        SELECT * FROM DocumentRevision 
        WHERE fileName = :targetFileName 
        ORDER BY revNumber DESC
    """)
     fun getFullRevisionHistory(targetFileName: String): Flow<List<DocumentRevision>>



    @Query("SELECT * FROM DocumentRevision WHERE filename = :filename ORDER BY revNumber DESC LIMIT 3")
    fun getLast3Versions(filename: String): Flow<List<DocumentRevision>>

    }

@Dao
interface DocumentFolderDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save (info: DocumentsFolder)

  @Query("SELECT * FROM DocumentsFolder")
   fun getAll() :Flow<List<DocumentsFolder>>

}

@Dao
interface ContentSearchDao {
@Transaction
@Insert
suspend fun insertContent (content :List < ContentSearch>)
}

