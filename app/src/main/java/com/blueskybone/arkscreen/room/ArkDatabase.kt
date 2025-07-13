package com.blueskybone.arkscreen.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blueskybone.arkscreen.room.dao.AccountGcDao
import com.blueskybone.arkscreen.room.dao.AccountSkDao
import com.blueskybone.arkscreen.room.dao.GachaDao
import com.blueskybone.arkscreen.room.dao.LinkDao

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Database(entities = [AccountSk::class, AccountGc::class, Link::class, Gacha::class], version = 4)
abstract class ArkDatabase : RoomDatabase() {
    abstract fun getAccountSkDao(): AccountSkDao
    abstract fun getAccountGcDao(): AccountGcDao
    abstract fun getLinkDao(): LinkDao
    abstract fun getGachaDao(): GachaDao

    companion object {

        private const val DatabaseName = "ArkDatabase"

        @Volatile
        private var INSTANCE: ArkDatabase? = null

        fun getDatabase(context: Context): ArkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArkDatabase::class.java,
                    DatabaseName
                ).addMigrations(Migration2).addMigrations(Migration3).addMigrations(Migration4).build()
                INSTANCE = instance
                instance
            }
        }

        object Migration2 : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE new_table (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uid TEXT NOT NULL, channelMasterId INTEGER NOT NULL, nickName TEXT NOT NULL, token TEXT NOT NULL, official INTEGER NOT NULL)")

                db.execSQL(
                    "INSERT INTO new_table (id, uid, channelMasterId, nickName, token, official) "
                            + "SELECT id, uid, CAST(channelMasterId AS INTEGER), nickName, token, official FROM AccountGc"
                )

                db.execSQL("DROP TABLE AccountGc")

                db.execSQL("ALTER TABLE new_table RENAME TO AccountGc")

            }
        }
        object Migration3: Migration(2,3){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE Gacha (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uid TEXT NOT NULL, ts INTEGER NOT NULL, pool TEXT NOT NULL, record TEXT NOT NULL)")
                db.execSQL("CREATE INDEX index_Gacha_uid ON Gacha(uid)")
            }
        }

        object Migration4: Migration(3,4){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Link ADD COLUMN icon TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}