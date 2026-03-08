package `in`.realtechsolns.papertrack.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ CompanyInfo::class, DocumentRevision::class,DocumentsFolder::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

   // abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun documentRevisionDao(): DocumentRevisionDao
    abstract fun documentsFolderDao(): DocumentsFolder
}