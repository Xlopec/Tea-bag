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

@file:Suppress("FunctionName")

package com.max.reader.app.env.storage.local

import android.content.Context
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.Document

interface HasMongoCollection {
    val collection: MongoCollection<Document>
}

fun MongoCollection(
    context: Context,
): HasMongoCollection = object : HasMongoCollection {

    override val collection: MongoCollection<Document> by lazy {
        Stitch.initialize(context)

        val client = Stitch.initializeAppClient(context.packageName)

        client.getServiceClient(LocalMongoDbService.clientFactory)
            .getDatabase("app")
            .getCollection("favorite")
            .apply { if (FavoriteIndex !in indexesNames) createIndexes() }
    }
}

private fun MongoCollection<out Document>.createIndexes() =
    createIndex(Index(), IndexOptions().name(FavoriteIndex))

private fun Index() = BsonDocument().apply { appendTextIndexes(FavoriteIndexFields) }

private fun BsonDocument.appendTextIndexes(
    indexes: Iterable<String>,
) = indexes.forEach { field -> append(field, BsonString("text")) }

private val MongoCollection<out Document>.indexesNames: List<String>
    get() = listIndexes().mapNotNull { d -> d["name"] as? String }.toList()

private const val FavoriteIndex = "favorite.index"

private val FavoriteIndexFields = listOf("title", "description", "author")
