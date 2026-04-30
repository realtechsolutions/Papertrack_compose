package `in`.realtechsolns.papertrack.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ CompanyInfo::class, DocumentRevision::class,DocumentsFolder::class, DocumentSearch::class ,
    ContentSearch::class           ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

   // abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun documentRevisionDao(): DocumentRevisionDao
    abstract fun documentsFolderDao(): DocumentFolderDao
    abstract fun contentSearchDao(): ContentSearchDao
    abstract fun documentSearchDao(): DocumentSearchDao
}