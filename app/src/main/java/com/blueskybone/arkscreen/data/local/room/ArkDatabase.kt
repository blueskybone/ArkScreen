package com.blueskybone.arkscreen.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blueskybone.arkscreen.data.local.room.dao.AccountEfDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountGcDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountSkDao
import com.blueskybone.arkscreen.data.local.room.dao.AttendanceRecordDao
import com.blueskybone.arkscreen.data.local.room.dao.GachaDao
import com.blueskybone.arkscreen.data.local.room.dao.EndfieldGachaDao
import com.blueskybone.arkscreen.data.local.room.dao.LinkDao

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
@Database(
    entities = [
        AccountSk::class,
        AccountGc::class,
        AccountEf::class,
        Link::class,
        Gacha::class,
        AttendanceRecord::class,
        EndfieldGachaRecord::class,
        EndfieldGachaPool::class,
    ],
    version = 10,
)
abstract class ArkDatabase : RoomDatabase() {
    abstract fun getAccountSkDao(): AccountSkDao
    abstract fun getAccountGcDao(): AccountGcDao
    abstract fun getAccountEfDao(): AccountEfDao
    abstract fun getLinkDao(): LinkDao
    abstract fun getGachaDao(): GachaDao
    abstract fun getAttendanceRecordDao(): AttendanceRecordDao
    abstract fun getEndfieldGachaDao(): EndfieldGachaDao

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
                ).addMigrations(Migration2).addMigrations(Migration3).addMigrations(Migration4)
                    .addMigrations(Migration5).addMigrations(Migration6).addMigrations(Migration7)
                    .addMigrations(Migration8).addMigrations(Migration9).addMigrations(Migration10)
                    .build()
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

        object Migration3 : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE Gacha (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, uid TEXT NOT NULL, ts INTEGER NOT NULL, pool TEXT NOT NULL, record TEXT NOT NULL)")
                db.execSQL("CREATE INDEX index_Gacha_uid ON Gacha(uid)")
            }
        }

        object Migration4 : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Link ADD COLUMN icon TEXT NOT NULL DEFAULT ''")
            }
        }

        object Migration5 : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE AccountGc ADD COLUMN akUserCenter TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE AccountGc ADD COLUMN xrToken TEXT NOT NULL DEFAULT ''")

                db.execSQL("DROP TABLE Gacha")
                db.execSQL(
                    """
            CREATE TABLE Gacha (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                poolId TEXT NOT NULL DEFAULT 'UN',
                poolCate TEXT NOT NULL DEFAULT 'UN',
                uid TEXT NOT NULL,
                ts INTEGER NOT NULL,
                pool TEXT NOT NULL,
                charName TEXT NOT NULL,
                charId TEXT NOT NULL,
                rarity INTEGER NOT NULL,
                isNew INTEGER NOT NULL,
                pos INTEGER NOT NULL DEFAULT 0
            )
        """
                )
                db.execSQL("CREATE UNIQUE INDEX index_Gacha_uid_ts_pos ON Gacha(uid, ts, pos)")
            }
        }

        object Migration6 : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `AccountEf` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `uid` TEXT NOT NULL, 
                `channelMasterId` TEXT NOT NULL, 
                `nickName` TEXT NOT NULL, 
                `token` TEXT NOT NULL, 
                `dId` TEXT NOT NULL, 
                `official` INTEGER NOT NULL, 
                `roleId` TEXT NOT NULL, 
                `serverId` TEXT NOT NULL
            )
        """.trimIndent()
                )
            }
        }

        object Migration7 : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `AttendanceRecord` (
                        `accountType` TEXT NOT NULL,
                        `accountUid` TEXT NOT NULL,
                        `lastAttemptTs` INTEGER NOT NULL,
                        `lastSuccessTs` INTEGER NOT NULL,
                        `lastError` TEXT NOT NULL,
                        PRIMARY KEY(`accountType`, `accountUid`)
                    )
                    """.trimIndent()
                )
            }
        }

        object Migration8 : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE AccountEf ADD COLUMN bindingUid TEXT")
            }
        }

        object Migration9 : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `EndfieldGachaRecord` (
                        `roleId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `seqId` TEXT NOT NULL,
                        `poolId` TEXT NOT NULL,
                        `poolName` TEXT NOT NULL,
                        `poolType` TEXT NOT NULL,
                        `itemId` TEXT NOT NULL,
                        `itemName` TEXT NOT NULL,
                        `rarity` INTEGER NOT NULL,
                        `gachaTs` TEXT NOT NULL,
                        `kind` TEXT NOT NULL,
                        `isNew` INTEGER NOT NULL,
                        `isFree` INTEGER NOT NULL,
                        PRIMARY KEY(`roleId`, `type`, `seqId`)
                    )
                """.trimIndent())
            }
        }

        object Migration10 : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE EndfieldGachaRecord ADD COLUMN poolVersion INTEGER")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `EndfieldGachaPool` (
                        `roleId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `poolId` TEXT NOT NULL,
                        `poolName` TEXT NOT NULL,
                        `up6Ids` TEXT NOT NULL,
                        `officialCount` INTEGER,
                        PRIMARY KEY(`roleId`, `type`, `poolId`)
                    )
                """.trimIndent())
            }
        }
    }
}
