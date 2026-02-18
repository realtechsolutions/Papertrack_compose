package `in`.realtechsolns.papertrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAll(): List<User>
}

@Dao
interface CompanyDao  {
    @Insert
    suspend fun insert(info: CompanyInfo)

    @Query("SELECT * FROM CompanyInfo")
    suspend fun getAll(): List<CompanyInfo>
}