package com.example

import androidx.room.*
import android.content.Context

@Entity(tableName = "workspace_items")
data class WorkspaceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val activityName: String,
    val page: Int,
    val col: Int,
    val row: Int,
    val container: Int // 0 for home screen
)

@Entity(tableName = "folder_info")
data class FolderInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Int,
    val page: Int,
    val col: Int,
    val row: Int
)

@Entity(tableName = "app_preferences")
data class AppPreference(
    @PrimaryKey val packageName: String,
    val isHidden: Boolean,
    val customLabel: String?
)

@Dao
interface WorkspaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkspaceItem)

    @Delete
    suspend fun delete(item: WorkspaceItem)

    @Query("SELECT * FROM workspace_items")
    suspend fun getAll(): List<WorkspaceItem>

    @Query("SELECT * FROM workspace_items WHERE container = :containerId")
    suspend fun getAllForContainer(containerId: Int): List<WorkspaceItem>

    @Query("DELETE FROM workspace_items WHERE container = 0")
    suspend fun clearHomeScreen()
}

@Database(entities = [WorkspaceItem::class, FolderInfo::class, AppPreference::class], version = 1, exportSchema = false)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "launcher_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
