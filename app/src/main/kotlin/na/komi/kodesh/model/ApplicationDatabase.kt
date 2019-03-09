package na.komi.kodesh.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.framework.AssetSQLiteOpenHelperFactory

@Database(entities = [Bible::class], version = 2, exportSchema = false)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao

    companion object {
        @Volatile
        private var instance: ApplicationDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): ApplicationDatabase = instance
                ?: Room.databaseBuilder(ctx.applicationContext, ApplicationDatabase::class.java, "kjv-pce-v2.db")
                        .openHelperFactory(AssetSQLiteOpenHelperFactory())
                        .fallbackToDestructiveMigration()
                        .build().also { instance = it }

        fun destroyInstance() {
            instance = null
        }
    }

}