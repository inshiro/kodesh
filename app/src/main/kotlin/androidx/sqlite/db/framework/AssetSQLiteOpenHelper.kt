/*
 * Copyright (C) 2016 The Android Open Source Project
 * Modifications (c) 2017 CommonsWare, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.sqlite.db.framework

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

internal class AssetSQLiteOpenHelper(context: Context, name: String, version: Int,
                                     callback: SupportSQLiteOpenHelper.Callback) : SupportSQLiteOpenHelper {
    private val delegate: AssetHelper

    init {
        delegate = createDelegate(context, name, version, callback)
    }

    private fun createDelegate(context: Context, name: String,
                               version: Int, callback: SupportSQLiteOpenHelper.Callback): AssetHelper {
        return object : AssetHelper(context, name, version) {
            override fun onCreate(db: SQLiteDatabase) {
                wrappedDb = FrameworkSQLiteDatabase(db)
                callback.onCreate(wrappedDb)
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                                   newVersion: Int) {
                callback.onUpgrade(getWrappedDb(db), oldVersion,
                        newVersion)
            }

            override fun onConfigure(db: SQLiteDatabase) {
                callback.onConfigure(getWrappedDb(db))
            }

            override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int,
                                     newVersion: Int) {
                callback.onDowngrade(getWrappedDb(db), oldVersion, newVersion)
            }

            override fun onOpen(db: SQLiteDatabase) {
                callback.onOpen(getWrappedDb(db))
            }
        }
    }

    override fun getDatabaseName(): String {
        return delegate.databaseName
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        delegate.setWriteAheadLoggingEnabled(enabled)
    }

    override fun getWritableDatabase(): SupportSQLiteDatabase {
        return delegate.writableSupportDatabase
    }

    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return delegate.readableSupportDatabase
    }

    override fun close() {
        delegate.close()
    }

    internal abstract class AssetHelper(context: Context, name: String, version: Int) : SQLiteAssetHelper(context, name, null, null, version, null) {
        var wrappedDb: FrameworkSQLiteDatabase? = null

        val writableSupportDatabase: SupportSQLiteDatabase
            get() {
                val db = super.getWritableDatabase()
                return getWrappedDb(db)
            }

        val readableSupportDatabase: SupportSQLiteDatabase
            get() {
                val db = super.getReadableDatabase()
                return getWrappedDb(db)
            }

        fun getWrappedDb(sqLiteDatabase: SQLiteDatabase): FrameworkSQLiteDatabase {
            if (wrappedDb == null) {
                wrappedDb = FrameworkSQLiteDatabase(sqLiteDatabase)
            }
            return wrappedDb as FrameworkSQLiteDatabase
        }

        @Synchronized
        override fun close() {
            super.close()
            wrappedDb = null
        }
    }
}
