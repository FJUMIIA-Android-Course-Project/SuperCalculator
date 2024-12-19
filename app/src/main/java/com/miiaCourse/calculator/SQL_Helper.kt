package com.miiaCourse.calculator

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQL_helpler(
    context: Context,
    name: String = database,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = ver
) : SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        private const val database = "HistoryList" //資料庫名稱
        private const val ver = 1 //資料庫版本
    }
    override fun onCreate(db: SQLiteDatabase) {
        //建立HistoryList 資料表，表內有HistoryRecord一個欄位
        db.execSQL("CREATE TABLE HistoryList(id text PRIMARY KEY, Record text not null)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {
        //升級資料庫版本時，刪除舊資料表，並重新執行 onCreate()，建立新資料表
        db.execSQL("DROP TABLE IF EXISTS HistoryList")
        onCreate(db)
    }
}