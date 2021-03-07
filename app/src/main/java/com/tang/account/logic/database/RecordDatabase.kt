package com.tang.account.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tang.account.model.Record

/*数据库类,版本号为1,实体类为Record*/
@Database(version = 1, entities = [Record::class])
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        /*数据库类实例*/
        private var instance: RecordDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): RecordDatabase {
            // 已经被实例化就直接返回实例
            instance?.let {
                return it
            }
            // 构造一个数据库类实例并返回
            return Room.databaseBuilder(
                context.applicationContext,
                RecordDatabase::class.java,
                "account_database"
            ).build().apply {
                instance = this
            }
        }
    }

}