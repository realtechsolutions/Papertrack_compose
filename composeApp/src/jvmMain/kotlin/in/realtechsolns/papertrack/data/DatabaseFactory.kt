package `in`.realtechsolns.papertrack.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import `in`.realtechsolns.papertrack.getDocumentDir
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
// val dbDir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"),"Papertrack")
//    dbDir.mkdirs()
    val dbDir = getDocumentDir()
    val dbFile = File(
        dbDir,
        "papertrack.db"
    )

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver()).fallbackToDestructiveMigration(true)
}