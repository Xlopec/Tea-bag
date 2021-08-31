/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("ObjectPropertyName")

package com.max.reader.app.env.storage.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DbVersion = 1
const val TableName = "favorite_articles"

// Columns
const val _Url = "url"
const val _Title = "title"
const val _Author = "author"
const val _Description = "description"
const val _UrlToImage = "urlToImage"
const val _Published = "published"
const val _IsFavorite = "isFavorite"

class DbHelper(context: Context) : SQLiteOpenHelper(context, "$TableName.db", null, DbVersion) {

    override fun onCreate(
        db: SQLiteDatabase,
    ) = db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TableName (
            $_Url TEXT PRIMARY KEY NOT NULL, 
            $_Title TEXT NOT NULL, 
            $_Author TEXT, 
            $_Description TEXT,
            $_UrlToImage TEXT,
            $_Published INTEGER NOT NULL,
            $_IsFavorite INTEGER NOT NULL
            )
        """.trimIndent())

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TableName")
        onCreate(db)
    }
}
