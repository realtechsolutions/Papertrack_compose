package `in`.realtechsolns.papertrack.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {

    val dbFile = File(
        System.getProperty("user.home"),
        "papertrack.db"
    )

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
}