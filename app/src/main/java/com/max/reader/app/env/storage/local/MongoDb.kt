/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
